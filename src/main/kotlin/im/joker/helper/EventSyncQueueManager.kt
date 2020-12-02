package im.joker.helper

import im.joker.constants.ImConstants.Companion.ACTIVE_ROOM_LATEST_EVENTS
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.other.FullReadMarkerEvent
import im.joker.event.room.other.ReceiptEvent
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component

@Component
class EventSyncQueueManager {

    private val log: Logger = LoggerFactory.getLogger(EventSyncQueueManager::class.java)

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    // 每个房间的事件队列为n条消息
    private val limit = 100

    @Autowired
    private lateinit var roomSubscribeManager: RoomSubscribeManager

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    /**
     * 将事件放入redis对应的room队列
     */
    suspend fun addToEventQueue(ev: AbstractRoomEvent) {
//        log.debug("将ev放入消息队列:{}", requestProcessor.toJson(ev))
        redisTemplate.opsForList().rightPush(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId), requestProcessor.toJson(ev)).awaitSingleOrNull()
        val queueSize = redisTemplate.opsForList().size(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId)).awaitSingle()
        // 当超过limit条事件的时候,修整该房间队列,删除最老的消息,以维持队列保证limit条
        if (limit > queueSize) {
            redisTemplate.opsForList().trim(ACTIVE_ROOM_LATEST_EVENTS.format(ev.roomId), queueSize - limit, -1).awaitSingleOrNull()
        }
    }

    /**
     * 从设备相关的房间中拿取limitOfRoom条数据,其返回值key是roomId,value是redis队列中提取的
     * 过滤其本人说的话
     */
    suspend fun takeRelatedEvent(deviceId: String, sender: String, gteStreamId: Long, lteStreamId: Long): Map<String, List<AbstractRoomEvent>> = coroutineScope {
        val roomIds = roomSubscribeManager.searchJoinRoomIds(deviceId)
        val list = roomIds.map {
            async {
                redisTemplate.opsForList().range(ACTIVE_ROOM_LATEST_EVENTS.format(it), 0, -1).collectList().awaitSingleOrNull()
            }
        }
        val eventMap = HashMap<String, List<AbstractRoomEvent>>()
        list.awaitAll().forEach { it1 ->
            val roomEvents = it1.map { requestProcessor.toBean(it, AbstractRoomEvent::class.java) }
                    .filter {
                        val range = it.streamId in gteStreamId..lteStreamId
                        if (it is FullReadMarkerEvent) {
                            return@filter range && it.sender == sender
                        }
                        range
                    }
            if (roomEvents.isNotEmpty()) eventMap[roomEvents[0].roomId] = roomEvents
        }
        return@coroutineScope eventMap
    }

}

