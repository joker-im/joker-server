package im.joker.helper

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import im.joker.repository.MongoStore
import im.joker.room.RoomState
import org.springframework.stereotype.Component
import java.time.Duration
import javax.annotation.PostConstruct

@Component
class RoomStateCache {

    private lateinit var roomStateCache: LoadingCache<String, RoomState>

    private lateinit var globalStateHolder: GlobalStateHolder

//    suspend fun xxx() {
//        RoomState.from("", globalStateHolder)
//
//        CacheBuilder.newBuilder()
//                .maximumSize(100)
//                .refreshAfterWrite(Duration.ofMinutes(10))
//                .build(CacheLoader.from { it ->
//                    RoomState.from(it, globalStateHolder)
//
//                })
//
//    })

    @PostConstruct
    fun test() {
        print("xxxx")
    }

}
