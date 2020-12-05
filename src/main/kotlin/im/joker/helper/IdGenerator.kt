package im.joker.helper

import im.joker.constants.ImConstants.Companion.EVENT_STREAM_SEQUENCE
import kotlinx.coroutines.reactive.awaitFirstOrDefault
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.incrementAndAwait
import org.springframework.stereotype.Component
import java.util.*

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:50
 * @Desc:
 */
@Component
class IdGenerator {

    val log: Logger = LoggerFactory.getLogger(IdGenerator::class.java)

    @Autowired
    lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Value("\${im.web-domain}")
    lateinit var webDomain: String


    suspend fun nextEventStreamId(): Long {
        log.debug("获取nextStreamId")
        return redisTemplate.opsForValue().incrementAndAwait(EVENT_STREAM_SEQUENCE)
    }

    suspend fun findLatestStreamId(): Long {
        return redisTemplate.opsForValue().get(EVENT_STREAM_SEQUENCE).awaitFirstOrDefault("0").toLong()
    }

    fun newRoomId(): String {
        return "!" + UUID.randomUUID().toString() + ":" + webDomain
    }

    fun roomVersion(): String {
        return "5"
    }

    fun userId(username: String): String {
        return "@$username:$webDomain"
    }


}