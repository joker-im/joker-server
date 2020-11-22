package im.joker.device

import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_AVATAR
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_DEVICE_ID
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_DEVICE_NAME
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USERNAME
import im.joker.constants.ImConstants.Companion.TOKEN_USER_HASH_KEY_USER_ID
import im.joker.constants.ImConstants.Companion.USER_DEVICES_TOKENS_HASH
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.RequestProcessor
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.*
import kotlin.collections.HashMap

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 18:22
 * @Desc:
 */
@Component
class DeviceManager {

    val log: Logger = LoggerFactory.getLogger(DeviceManager::class.java)


    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    suspend fun findOrCreateDevice(
            username: String, deviceId: String,
            userId: String, deviceName: String?,
            deviceAvatar: String?
    ): Device {
        log.debug("判断username:{}是否存在redis的hash中", username)
        val token =
                redisTemplate.opsForHash<String, String>()
                        .get(USER_DEVICES_TOKENS_HASH.format(username), username).awaitSingleOrNull()
        return if (token.isNullOrEmpty()) {
            log.debug("username:{} token为空,创建新token", username)
            createNewToken(deviceId, username, deviceName, userId, deviceAvatar)
        } else {
            val entries =
                    redisTemplate.opsForHash<String, String>().entries(TOKEN_USER_HASH.format(token)).awaitSingleOrNull()
            log.info("发现已存在token信息:{}", entries)
            requestProcessor.toBean(requestProcessor.toJson(entries), Device::class.java)
        }
    }

    suspend fun createNewToken(deviceId: String, username: String, deviceName: String?, userId: String, deviceAvatar: String?): Device = coroutineScope {
        val token = UUID.randomUUID().toString()
        val duration = Duration.ofDays(7L)
        val tokenDeviceMap = HashMap<String, String>()
        val deviceTokenMap = HashMap<String, String>()
        deviceTokenMap[deviceId] = token
        tokenDeviceMap[TOKEN_USER_HASH_KEY_USERNAME] = username
        tokenDeviceMap[TOKEN_USER_HASH_KEY_DEVICE_ID] = deviceId
        tokenDeviceMap[TOKEN_USER_HASH_KEY_USER_ID] = userId
        deviceName?.let {
            tokenDeviceMap[TOKEN_USER_HASH_KEY_DEVICE_NAME] = it
        }
        deviceAvatar?.let {
            tokenDeviceMap[TOKEN_USER_HASH_KEY_AVATAR] = it
        }
        val async1 = async {
            redisTemplate.opsForHash<String, String>().putAll(USER_DEVICES_TOKENS_HASH.format(username), deviceTokenMap)
                    .then(redisTemplate.expire(USER_DEVICES_TOKENS_HASH.format(username), duration)).awaitSingleOrNull()
        }
        val async2 = async {
            redisTemplate.opsForHash<String, String>().putAll(TOKEN_USER_HASH.format(token), tokenDeviceMap)
                    .then(redisTemplate.expire(TOKEN_USER_HASH.format(token), duration)).awaitSingleOrNull()
        }
        async1.await()
        async2.await()
        val device = Device(deviceId, token, username, deviceName, deviceAvatar, userId)
        log.info("创建新device:{}", device)
        return@coroutineScope device
    }

    fun find(token: String): Mono<Device> {
        return redisTemplate.opsForHash<String, String>().entries(java.lang.String.format(TOKEN_USER_HASH, token))
                .switchIfEmpty(Mono.error(ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)))
                .collectMap({ it.key }, { it.value })
                .map {
                    Device(it[TOKEN_USER_HASH_KEY_DEVICE_ID]!!,
                            token,
                            it[TOKEN_USER_HASH_KEY_USERNAME]!!,
                            it[TOKEN_USER_HASH_KEY_DEVICE_NAME],
                            it[TOKEN_USER_HASH_KEY_AVATAR],
                            it[TOKEN_USER_HASH_KEY_USER_ID]!!
                    )
                }

    }

    suspend fun removeDevice(device: Device): Unit = coroutineScope {
        val async1 = async {
            redisTemplate.delete(TOKEN_USER_HASH.format(device.accessToken)).awaitSingleOrNull()
        }
        val async2 = async {
            redisTemplate.opsForHash<String, String>()
                    .remove(USER_DEVICES_TOKENS_HASH.format(device.username), device.deviceId).awaitSingleOrNull()
        }
        async1.await()
        async2.await()
    }


    suspend fun findDeviceTokens(username: String): List<Device> {
        val deviceTokenList = redisTemplate.opsForHash<String, String>()
                .entries(USER_DEVICES_TOKENS_HASH.format(username))
                .collectList().awaitSingleOrNull()
        return deviceTokenList.map {
            Device(it.key, it.value, username, "", "", "")
        }.toList()

    }

    suspend fun deleteAllDevice(deviceTokens: List<Device>) {
        deviceTokens.forEach { removeDevice(it) }
    }


}