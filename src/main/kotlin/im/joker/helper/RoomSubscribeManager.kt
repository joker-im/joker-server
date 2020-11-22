package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_SUBSCRIBERS_OF_DEVICE
import im.joker.device.Device
import im.joker.presence.PresenceType
import im.joker.room.RoomManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.ScanOptions
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
                roomSubscribeKeys.forEach {
                    // 查看deviceId是不是每个房间的成员
                    val member = redisTemplate.opsForSet().isMember(it, device.deviceId).awaitSingleOrNull()
                    if (member) {
                        redisTemplate.opsForSet().remove(it, device.deviceId).awaitSingleOrNull()
                    }
                }

            }
            PresenceType.ONLINE -> {
                val joinRoomIds = roomManager.searchJoinRoomIdsFromDb(device.userId)
                val asyncList = ArrayList<Deferred<Long>>()
                joinRoomIds.forEach {
                    val async = async {
                        redisTemplate.opsForSet().add(ROOM_SUBSCRIBERS_OF_DEVICE.format(it), device.deviceId)
                                .awaitSingleOrNull()
                    }
                    asyncList.add(async)
                }
                asyncList.forEach {
                    it.await()
                }
            }

        }
    }


    /**
     * 实时查询device关心的房间id
     */
    suspend fun searchJoinRoomIds(deviceId: String): List<String> {
        val scanOptions = ScanOptions.scanOptions().match(ROOM_SUBSCRIBERS_OF_DEVICE.format("*"))
                .build()
        // 先得到所有的房间
        val roomSubscribeKeys = redisTemplate.scan(scanOptions).collectList().awaitSingleOrNull()
        val roomIds = ArrayList<String>()
        roomSubscribeKeys.forEach {
            // 查看deviceId是不是每个房间的成员
            val member = redisTemplate.opsForSet().isMember(it, deviceId).awaitSingleOrNull()
            if (member) {
                roomIds.add(it.replace(ROOM_SUBSCRIBERS_OF_DEVICE.format(""), ""))
            }
        }
        return roomIds
    }

}