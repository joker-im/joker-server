package im.joker.helper

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import im.joker.constants.ImConstants
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.room.Room
import im.joker.handler.RoomHandler
import im.joker.room.RoomState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.redisson.api.RedissonReactiveClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
class ImCache {

    private lateinit var roomStateCache: LoadingCache<String, Deferred<RoomState>>

    private lateinit var roomCache: LoadingCache<String, Deferred<Room>>

    @Autowired
    private lateinit var globalStateHolder: GlobalStateHolder

    private val log: Logger = LoggerFactory.getLogger(ImCache::class.java)

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @Autowired
    private lateinit var redissonClient: RedissonReactiveClient

    @Autowired
    private lateinit var roomHandler: RoomHandler


    @PostConstruct
    private fun init() {
        log.info("初始化imCache...")
        roomStateCache = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build(CacheLoader.from<String, Deferred<RoomState>> {
                    coroutineScope.async {
                        RoomState.from(it ?: "", globalStateHolder)
                    }
                })

        roomCache = CacheBuilder.newBuilder()
                .maximumSize(2000)
                .expireAfterAccess(Duration.ofMinutes(10))
                .build(CacheLoader.from<String, Deferred<Room>> {
                    coroutineScope.async {
                        it ?: Room()
                        roomHandler.findRoom(it!!)
                    }
                })

        redissonClient.getTopic(ImConstants.ROOM_STATE_TOPIC).addListener(String::class.java) { _, msg ->
            log.debug("收到房间状态信息变动,roomId:{}", msg)
            expireRoomState(msg)
        }.subscribe()
    }

    suspend fun getRoom(roomId: String): Room {
        return roomCache[roomId].await()
    }

    suspend fun getRoomState(roomId: String): RoomState {
        return roomStateCache[roomId].await()
    }

    fun expireRoomState(roomId: String) {
        roomStateCache.refresh(roomId)
    }

    fun notifyStateChange(ev: AbstractRoomEvent) {
        if (ev is AbstractRoomStateEvent) redissonClient.getTopic(ImConstants.ROOM_STATE_TOPIC).publish(ev.roomId)
    }

}
