package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_SUBSCRIBERS_OF_DEVICE_SET
import im.joker.device.Device
import im.joker.device.DeviceManager
import im.joker.event.EventType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.handler.RoomHandler
import im.joker.presence.PresenceType
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrNull
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
    private lateinit var roomHandler: RoomHandler

    @Autowired
    private lateinit var deviceManager: DeviceManager

    @Autowired
    private lateinit var roomMessageHelper: RoomMessageHelper

    /**
     * 用户上线时,需调用此方法, 将自己的deviceId注册到感兴趣的房间
     */
    suspend fun updateRelation(device: Device, type: PresenceType): Unit = coroutineScope {
        log.info("更新设备房间订阅:deviceId:{},PresenceType:{}", device.deviceId, type.id)
        when (type) {
            PresenceType.OFFLINE -> {
                val scanOptions = ScanOptions.scanOptions().match(ROOM_SUBSCRIBERS_OF_DEVICE_SET.format("*"))
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
                val joinRoomIds = roomHandler.searchRelatedRoomFromDb(device.userId)
                val asyncList = ArrayList<Deferred<Long>>()
                joinRoomIds.forEach {
                    val async = async {
                        redisTemplate.opsForSet().addAndAwait(ROOM_SUBSCRIBERS_OF_DEVICE_SET.format(it), device.deviceId)
                    }
                    asyncList.add(async)
                }
                asyncList.awaitAll()
            }

            else -> return@coroutineScope
        }
    }


    suspend fun updateRelation(ev: AbstractRoomEvent) {
        // 如果不是membership事件,那么无需更新房间订阅关系
        if (!EventType.Membership.`is`(ev.type)) {
            return
        }
        ev as MembershipEvent
        // 先把所有涉及到的人加入到订阅上
        val deviceIds = deviceManager.findDeviceIdsByUserId(ev.sender)
        var beOperatedDeviceIds: Set<String>? = null
        if (ev.sender != ev.stateKey) {
            beOperatedDeviceIds = deviceManager.findDeviceIdsByUserId(ev.stateKey)
            deviceIds.addAll(beOperatedDeviceIds)
        }
        for (deviceId in deviceIds) {
            redisTemplate.opsForSet().addAndAwait(ROOM_SUBSCRIBERS_OF_DEVICE_SET.format(ev.roomId), deviceId)
            // 清除当前人的最大可读数,如果存在的话
            roomMessageHelper.clearMaxStreamId(ev.sender, ev.roomId)
        }
        // 对他人做出操作,他人设置最大可读数
        beOperatedDeviceIds?.let {
            roomMessageHelper.setMaxStreamId(ev.stateKey, ev.roomId, ev.streamId)
        }

    }


    /**
     * 查询该房间的订阅的设备id
     */
    suspend fun searchRoomSubscriber(roomId: String): List<String> {
        return redisTemplate.opsForSet().members(ROOM_SUBSCRIBERS_OF_DEVICE_SET.format(roomId)).collectList().awaitSingleOrNull()
    }

    // todo 定时将每个房间中最小的event StreamId 与每个userId的最大可读的数进行对比,如果比可读数要大,那么T出订阅房间

}