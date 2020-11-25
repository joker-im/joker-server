package im.joker.auth

import im.joker.api.vo.account.LoginRequest
import im.joker.api.vo.account.LoginResponse
import im.joker.api.vo.presence.PresenceRequest
import im.joker.device.Device
import im.joker.device.DeviceManager
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.handler.PresenceHandler
import im.joker.helper.IdGenerator
import im.joker.helper.PasswordEncoder
import im.joker.presence.PresenceType
import im.joker.repository.MongoStore
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.util.*

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 17:34
 * @Desc:
 */
@Component
class AuthManager {

    private val log: Logger = LoggerFactory.getLogger(AuthManager::class.java)

    @Autowired
    private lateinit var mongodbStore: MongoStore

    @Autowired
    private lateinit var deviceManager: DeviceManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var presenceHandler: PresenceHandler

    @Autowired
    private lateinit var idGenerator: IdGenerator


    suspend fun login(request: LoginRequest): LoginResponse {
        log.info("收到登录请求:user:{},deviceId:{}", request.identifier.user, request.deviceId)
        request.deviceId ?: let {
            request.deviceId = UUID.randomUUID().toString()
        }
        val user = mongodbStore.findUserByUsername(request.identifier.user)
                ?: throw ImException(ErrorCode.INVALID_USERNAME, HttpStatus.FORBIDDEN)
        if (passwordEncoder.matches(passwordEncoder.encode(request.password), user.password)) {
            throw ImException(ErrorCode.CAPTCHA_INVALID, HttpStatus.FORBIDDEN)
        }
        val device = deviceManager.findOrCreateDevice(user.username, request.deviceId, user.userId,
                request.initialDeviceDisplayName, user.avatarUrl, user.displayName)
        val presenceRequest = PresenceRequest()
        presenceRequest.presence = PresenceType.ONLINE.id
        presenceHandler.setPresence(presenceRequest, device)
        return LoginResponse().apply {
            accessToken = device.accessToken
            deviceId = device.deviceId
            userId = device.userId
        }

    }

    suspend fun logout(device: Device) {
        log.info("收到设备登出请求.用户:{},设备:{}", device.userId, device.deviceId)
        val presenceRequest = PresenceRequest()
        presenceRequest.presence = PresenceType.OFFLINE.id
        presenceHandler.setPresence(presenceRequest, device)
        deviceManager.removeDevice(device)
    }

    suspend fun logoutAll(device: Device) {
        log.info("收到设备登出全部请求,其username:{},deviceId:{}", device.username, device.deviceId)
        val devices = deviceManager.findDeviceTokens(device.username)
        val presenceRequest = PresenceRequest()
        presenceRequest.presence = PresenceType.ONLINE.id
        devices.forEach {
            presenceHandler.setPresence(presenceRequest, it)
        }
        deviceManager.deleteAllDevice(devices)
    }

}