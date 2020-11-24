package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_STATE_TOPIC
import im.joker.constants.ImConstants.Companion.ROOM_SUBSCRIBERS_OF_DEVICE
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.presence.PresenceType
import im.joker.room.RoomManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.redisson.api.RedissonReactiveClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 15:38
 * @Desc:
 */
@Component
class RoomSubscribeManager {

    private var log: Logger = LoggerFactory.getLogger(RoomSubscribeManager::class.java)

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Autowired
    private lateinit var roomManager: RoomManager

    /**
     * 用户上线时,需调用此方法, 将自己的deviceId注册到感兴趣的房间
     */
    suspend fun updateRelation(device: Device, type: PresenceType): Unit = coroutineScope {
        log.info("更新设备房间订阅:deviceId:{},PresenceType:{}", device.deviceId, type.id)
        when (type) {
            PresenceType.OFFLINE, PresenceType.UNAVAILABLE -> {
                val scanOptions = ScanOptions.scanOptions().match(ROOM_SUBSCRIBERS_OF_DEVICE.format("*"))
                        .build()
                // 先得到所有的房间
                val roomSubscribeKeys = redisTemplate.scan(scanOptions).collectList().awaitSingleOrNull()
                val asyncList = ArrayList<Deferred<Any>>()
                // 每个房间删除该deviceId
                roomSubscribeKeys.forEach {
                    val async = async {
                        // 查看deviceId是不是每个房间的成员
                        val member = redisTemplate.opsForSet().isMember(it, device.deviceId).awaitSingleOrNull()
                        if (member) {
                            redisTemplate.opsForSet().removeAndAwait(it, device.deviceId)
                        }
                    }
                    asyncList.add(async)
                }
                asyncList.awaitAll()
            }
            PresenceType.ONLINE -> {
                val joinRoomIds = roomManager.searchJoinRoomIdsFromDb(device.userId)
                val asyncList = ArrayList<Deferred<Long>>()
                joinRoomIds.forEach {
                    val async = async {
                        redisTemplate.opsForSet().addAndAwait(ROOM_SUBSCRIBERS_OF_DEVICE.format(it), device.deviceId)
                    }
                    asyncList.add(async)
                }
                asyncList.awaitAll()
            }

        }
    }


    suspend fun updateRelation(device: Device, ev: AbstractRoomEvent) {
        // 如果不是membership事件,那么无需更新房间订阅关系
        if (!EventType.Membership.`is`(ev.type) && ev is AbstractRoomStateEvent) {
            return
        }
        val evMembershipType = (ev as MembershipEvent).content.membership

        if (MembershipType.Join.`is`(evMembershipType) || MembershipType.Invite.`is`(evMembershipType)) {
            redisTemplate.opsForSet().addAndAwait(ROOM_SUBSCRIBERS_OF_DEVICE.format(ev.roomId), device.deviceId)
        }
        if (MembershipType.Ban.`is`(evMembershipType) || MembershipType.Leave.`is`(evMembershipType)) {
            redisTemplate.opsForSet().removeAndAwait(ROOM_SUBSCRIBERS_OF_DEVICE.format(ev.roomId), device.deviceId)
        }

    }


    /**
     * 实时查询device关心的房间id
     */
    suspend fun searchJoinRoomIds(deviceId: String): List<String> = coroutineScope {
        val scanOptions = ScanOptions.scanOptions().match(ROOM_SUBSCRIBERS_OF_DEVICE.format("*"))
                .build()
        // 先得到所有的房间
        val roomSubscribeKeys = redisTemplate.scan(scanOptions).collectList().awaitSingleOrNull()
        val roomIds = ArrayList<String>()
        val asyncList = ArrayList<Deferred<Any>>()
        roomSubscribeKeys.forEach {
            val async = async {
                // 查看deviceId是不是每个房间的成员
                val member = redisTemplate.opsForSet().isMemberAndAwait(it, deviceId)
                if (member) {
                    roomIds.add(it.replace(ROOM_SUBSCRIBERS_OF_DEVICE.format(""), ""))
                }
            }
            asyncList.add(async)
        }
        asyncList.awaitAll()
        return@coroutineScope roomIds
    }


}