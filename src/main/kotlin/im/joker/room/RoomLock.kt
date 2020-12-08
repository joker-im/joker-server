package im.joker.room

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RoomLock {

    private val lockMap = ConcurrentHashMap<String, Mutex>()

    private val lock = Mutex()

    suspend fun getRoomLock(roomId: String): Mutex {
        var mutex = lockMap[roomId]
        if (mutex == null) {
            lock.withLock {
                mutex = Mutex()
                lockMap[roomId] = mutex!!
            }
        }
        return mutex!!
    }
}