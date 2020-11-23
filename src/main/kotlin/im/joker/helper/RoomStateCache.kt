package im.joker.helper

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import im.joker.room.RoomState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RoomStateCache {

    private var roomStateCache = CacheBuilder.newBuilder()
            .maximumSize(2000)
            .refreshAfterWrite(Duration.ofMinutes(1))
            .build(CacheLoader.from<String, Deferred<RoomState>> {
                CoroutineScope(Dispatchers.IO).async {
                    RoomState.from(it!!, globalStateHolder)
                }
            })

    @Autowired
    private lateinit var globalStateHolder: GlobalStateHolder


    suspend fun getRoomState(roomId: String): RoomState {
        return roomStateCache[roomId].await()
    }

}
