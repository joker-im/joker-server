package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_SYNC_DEVICE
import kotlinx.coroutines.channels.Channel
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

    suspend fun notifySyncDevice(deviceId: String) {
        redissonReactiveClient.getTopic(ROOM_SYNC_DEVICE).publish(deviceId).awaitSingleOrNull()
    }

    fun removeWaitingDevice(deviceId: String) {
        val remove = waitingSyncMap.remove(deviceId)
        remove?.close()
    }

}