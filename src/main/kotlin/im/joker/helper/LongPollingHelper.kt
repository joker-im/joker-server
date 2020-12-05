package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_SYNC_DEVICE
import im.joker.device.Device
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.other.FullReadMarkerEvent
import im.joker.event.room.other.ReceiptEvent
import im.joker.event.room.other.TypingEvent
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
            log.debug("唤醒device:{}", deviceId)
            waitingSyncMap[deviceId]?.offer(true)
            removeWaitingDevice(deviceId)
        }.subscribe()
    }


    fun addWaitingDevice(deviceId: String, channel: Channel<Boolean>) {
        waitingSyncMap[deviceId] = channel
    }

    suspend fun notifySyncDevice(event: AbstractRoomEvent, device: Device): Unit = coroutineScope {
        val deviceIds = roomSubscribeManager.searchRoomSubscriber(event.roomId)
                .filter {
                    // 如果是打字事件,回执事件不唤醒自己
                    if (event is TypingEvent || event is ReceiptEvent || event is FullReadMarkerEvent) it != device.deviceId else true
                }

        val deferredList = deviceIds
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