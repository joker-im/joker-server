package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_SYNC_DEVICE
import im.joker.device.Device
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.redisson.api.RedissonReactiveClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import javax.annotation.PostConstruct

@Component
class LongPollingHelper {

    private val log: Logger = LoggerFactory.getLogger(LongPollingHelper::javaClass.name)


    @Autowired
    private lateinit var redissonReactiveClient: RedissonReactiveClient


    private val waitingSyncMap = ConcurrentHashMap<String, Channel<Boolean>>()

    @Autowired
    private lateinit var roomSubscribeManager: RoomSubscribeManager


    @PostConstruct
    fun init() {
        redissonReactiveClient.getTopic(ROOM_SYNC_DEVICE).addListener(String::class.java) { _, deviceId ->
            log.info("唤醒device:{}", deviceId)
            waitingSyncMap[deviceId]?.offer(true)
            removeWaitingDevice(deviceId)
        }.subscribe()
    }


    fun addWaitingDevice(deviceId: String, channel: Channel<Boolean>) {
        waitingSyncMap[deviceId] = channel
    }

    suspend fun notifySyncDevice(event: AbstractRoomEvent, device: Device): Unit = coroutineScope {
        val deviceIds = roomSubscribeManager.searchRoomSubscriber(event.roomId)
        // 自己发的事件不唤醒自己,只唤醒别人.(除了状态事件)
        val deferredList = deviceIds
                .filter {
                    event is AbstractRoomStateEvent || it != device.deviceId
                }
                .map {
                    async { redissonReactiveClient.getTopic(ROOM_SYNC_DEVICE).publish(it).awaitSingleOrNull() }
                }
        deferredList.awaitAll()
    }

    fun removeWaitingDevice(deviceId: String) {
        val remove = waitingSyncMap.remove(deviceId)
        remove?.close()
    }

}