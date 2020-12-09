package im.joker.helper

import im.joker.constants.ImConstants
import im.joker.constants.ImConstants.Companion.ACTIVE_ROOM_LATEST_EVENTS
import im.joker.constants.ImConstants.Companion.ROOM_TYPING_FLAG_SET
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.content.other.TypingContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.other.TypingEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrDefault
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Range
import org.springframework.data.redis.core.*
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class SyncEventManager {


    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    // 每个房间的事件队列为n条消息
    private val limit = 50L

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    /**
     * 将事件放入redis对应的room队列
     */
    suspend fun addToEventQueue(ev: AbstractRoomEvent, device: Device) {
        if (ev is TypingEvent) {
            setTypingEvent(ev, device)
            return
        }

        redisTemplate.opsForList().rightPushAndAwait(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId), requestProcessor.toJson(ev))
        val queueSize = redisTemplate.opsForList().sizeAndAwait(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId))
        // 当超过limit条事件的时候,修整该房间队列,删除最老的消息,以维持队列保证limit条
        if (queueSize > limit) {
            redisTemplate.opsForList().trimAndAwait(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId), -1 * limit, -1)
        }
    }

    /**
     * 从设备相关的房间中拿取limitOfRoom条数据,其返回值key是roomId,value是redis队列中提取的,不包含typingEvent
     * 过滤其本人说的话
     */
    suspend fun takeRelatedEvent(roomIds: List<String>, device: Device, gteStreamId: Long, lteStreamId: Long): HashMap<String, List<AbstractRoomEvent>> = coroutineScope {
        val list = roomIds.map {
            async {
                redisTemplate.opsForList().range(ACTIVE_ROOM_LATEST_EVENTS.format(it), -1 * limit, -1).collectList().awaitSingleOrNull()
            }
        }
        val eventMap = HashMap<String, List<AbstractRoomEvent>>()
        list.awaitAll().forEach { eventJson ->
            var currentRoomId: String? = null
            val roomEvents = eventJson.map { requestProcessor.toBean(it, AbstractRoomEvent::class.java) }
                    .filter {
                        if (currentRoomId == null) {
                            currentRoomId = it.roomId
                        }
                        it.streamId in gteStreamId..lteStreamId
                    }
                    .toMutableList()
            takeTypingEvent(currentRoomId!!, device)?.let {
                roomEvents.add(it)
            }
            if (roomEvents.isNotEmpty()) {
                eventMap[currentRoomId!!] = roomEvents
            }
        }
        return@coroutineScope eventMap
    }

    suspend fun setTypingEvent(ev: TypingEvent, device: Device) {
        redisTemplate.deleteAndAwait(ROOM_TYPING_FLAG_SET.format(ev.roomId))
        if (ev.content.typing) {
            redisTemplate.opsForZSet().addAndAwait(ImConstants.ROOM_TYPING_USER_Z_SET.format(ev.roomId), ev.sender,
                    (ev.content.timeout + System.currentTimeMillis()).toDouble())
        } else {
            redisTemplate.opsForZSet().removeAndAwait(ImConstants.ROOM_TYPING_USER_Z_SET.format(ev.roomId), ev.sender)
        }

    }

    suspend fun takeTypingEvent(roomId: String, device: Device): TypingEvent? {
        // 除非该房间又有新的打字事件,否则每个设备只能拿一次
        if (!redisTemplate.opsForSet().isMemberAndAwait(ROOM_TYPING_FLAG_SET.format(roomId), device.deviceId)) {
            redisTemplate.opsForSet().addAndAwait(ROOM_TYPING_FLAG_SET.format(roomId), device.deviceId)
            redisTemplate.expireAndAwait(ROOM_TYPING_FLAG_SET.format(roomId), Duration.ofMinutes(5))
        } else {
            return null
        }
        val users = redisTemplate.opsForZSet().rangeByScore(ImConstants.ROOM_TYPING_USER_Z_SET.format(roomId),
                Range.rightUnbounded(Range.Bound.exclusive(System.currentTimeMillis().toDouble())))
                .collectList().awaitSingleOrDefault(ArrayList())
        return TypingEvent().apply {
            this.content = TypingContent().apply {
                this.userIds = users.toSet()
            }
            this.roomId = roomId
            type = EventType.Typing.id
        }
    }

}

