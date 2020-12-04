package im.joker.helper

import im.joker.constants.ImConstants.Companion.ROOM_MESSAGE_LIMIT_HASH
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.putAndAwait
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.stereotype.Service

@Service
class RoomMessageHelper {

    private val log: Logger = LoggerFactory.getLogger(RoomMessageHelper::class.java)

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate


    /**
     * 设置用户在每个房间中能读取的最大的maxStreamId
     */
    suspend fun setMaxStreamId(userId: String, roomId: String, limit: Long) {
        redisTemplate.opsForHash<String, String>().putAndAwait(ROOM_MESSAGE_LIMIT_HASH.format(userId), roomId, limit.toString())
    }

    /**
     * 获取该用户在该房间能读取的最大maxStreamId
     */
    suspend fun getRoomMaxStreamId(userId: String, roomId: String): Long? {
        val streamId: String? = redisTemplate.opsForHash<String, String>().getAndAwait(ROOM_MESSAGE_LIMIT_HASH.format(userId), roomId)
        return streamId?.toLong()
    }


    /**
     * 清除最大可读数
     */
    suspend fun clearMaxStreamId(userId: String, roomId: String) {
        redisTemplate.opsForHash<String, String>().removeAndAwait(ROOM_MESSAGE_LIMIT_HASH.format(userId), roomId)
    }

}