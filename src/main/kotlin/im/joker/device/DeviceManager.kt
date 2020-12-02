package im.joker.device

import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USER_AVATAR
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_DEVICE_ID
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_DEVICE_NAME
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USERNAME
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USER_DISPLAY_NAME
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USER_ID
import im.joker.constants.ImConstants.Companion.USER_DEVICES_TOKENS_HASH
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.RequestProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.data.redis.core.deleteAndAwait
import org.springframework.data.redis.core.getAndAwait
import org.springframework.data.redis.core.removeAndAwait
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 18:22
 * @Desc:
 */
@Component
class DeviceManager {

    private val log: Logger = LoggerFactory.getLogger(DeviceManager::class.java)


    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    suspend fun findOrCreateDevice(
            username: String, deviceId: String,
            userId: String, deviceName: String,
            userAvatar: String,
            userDisplayName: String
    ): Device {
        log.debug("判断userId:{}是否存在redis的hash中", userId)
        val token = redisTemplate.opsForHash<String, String>().get(USER_DEVICES_TOKENS_HASH.format(userId), deviceId).awaitSingleOrNull()
        return if (token.isNullOrEmpty()) {
            log.debug("userId:{} token为空,创建新token", username)
            createNewToken(deviceId, username, deviceName, userId, userAvatar, userDisplayName)
        } else {
            val entries =
                    redisTemplate.opsForHash<String, String>().entries(TOKEN_USER_HASH.format(token)).awaitSingleOrNull()
            log.info("发现已存在token信息:{}", entries)
            requestProcessor.toBean(requestProcessor.toJson(entries), Device::class.java)
        }
    }

    suspend fun createNewToken(deviceId: String, username: String, deviceName:
    String, userId: String, userAvatar: String, userDisplayName: String): Device = coroutineScope {
        val token = UUID.randomUUID().toString()
        val duration = Duration.ofDays(7L)
        val tokenDeviceMap = mapOf(
                TOKEN_USER_HASH_KEY_USERNAME to username,
                TOKEN_USER_HASH_KEY_USER_ID to userId,
                TOKEN_USER_HASH_KEY_USER_AVATAR to userAvatar,
                TOKEN_USER_HASH_KEY_DEVICE_NAME to deviceName,
                TOKEN_USER_HASH_KEY_USER_DISPLAY_NAME to userDisplayName,
                TOKEN_USER_HASH_KEY_DEVICE_ID to deviceId
        )
        val deviceTokenMap = mapOf(deviceId to token)

        val asyncList = listOf(
                async {
                    redisTemplate.opsForHash<String, String>().putAll(USER_DEVICES_TOKENS_HASH.format(userId), deviceTokenMap)
                            .then(redisTemplate.expire(USER_DEVICES_TOKENS_HASH.format(userId), duration)).awaitSingleOrNull()
                },
                async {
                    redisTemplate.opsForHash<String, String>().putAll(TOKEN_USER_HASH.format(token), tokenDeviceMap)
                            .then(redisTemplate.expire(TOKEN_USER_HASH.format(token), duration)).awaitSingleOrNull()
                }
        )
        asyncList.awaitAll()
        val device = Device(deviceId, token, username, deviceName, userAvatar, userId, userDisplayName)
        log.info("创建新device:{}", device)
        return@coroutineScope device
    }

    fun find(token: String): Mono<Device> {
        return redisTemplate.opsForHash<String, String>().entries(TOKEN_USER_HASH.format(token))
                .switchIfEmpty(Mono.error(ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)))
                .collectMap({ it.key }, { it.value })
                .map {
                    Device(it[TOKEN_USER_HASH_KEY_DEVICE_ID]!!,
                            token,
                            it[TOKEN_USER_HASH_KEY_USERNAME]!!,
                            it[TOKEN_USER_HASH_KEY_DEVICE_NAME]!!,
                            it[TOKEN_USER_HASH_KEY_USER_AVATAR]!!,
                            it[TOKEN_USER_HASH_KEY_USER_ID]!!,
                            it[TOKEN_USER_HASH_KEY_USER_DISPLAY_NAME]!!
                    )
                }

    }

    suspend fun removeDevice(device: Device): Unit = coroutineScope {
        val asyncList = listOf(
                async {
                    redisTemplate.deleteAndAwait(TOKEN_USER_HASH.format(device.accessToken))
                }, async {
            redisTemplate.opsForHash<String, String>()
                    .removeAndAwait(USER_DEVICES_TOKENS_HASH.format(device.username), device.deviceId)
        })
        asyncList.awaitAll()
    }


    suspend fun findDevices(device: Device): List<Device> {
        val deviceTokenList = redisTemplate.opsForHash<String, String>()
                .entries(USER_DEVICES_TOKENS_HASH.format(device.userId))
                .collectList().awaitSingleOrNull()
        return deviceTokenList.map {
            Device(it.key, it.value, device.username, device.name, device.userAvatar, device.userId, device.userDisplayName)
        }

    }

    suspend fun findDeviceIdsByUserId(userId: String): MutableSet<String> {
        val devicesTokens = redisTemplate.opsForHash<String, String>()
                .entries(USER_DEVICES_TOKENS_HASH.format(userId))
                .collectMap({ it.key }, { it.value }).awaitSingleOrNull()
        return devicesTokens.keys.toMutableSet()

    }

    suspend fun deleteAllDevice(deviceTokens: List<Device>) {
        deviceTokens.forEach { removeDevice(it) }
    }


}