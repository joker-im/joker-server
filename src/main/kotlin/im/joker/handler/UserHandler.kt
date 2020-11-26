package im.joker.handler

import im.joker.api.vo.account.LoginRequest
import im.joker.api.vo.account.LoginResponse
import im.joker.api.vo.account.RegisterRequest
import im.joker.api.vo.account.RegisterResponse
import im.joker.auth.AuthManager
import im.joker.device.Device
import im.joker.device.DeviceManager
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.IdGenerator
import im.joker.helper.PasswordEncoder
import im.joker.helper.RequestProcessor
import im.joker.repository.MongoStore
import im.joker.user.User
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:49
 * @Desc:
 */
@Component
class UserHandler {

    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    @Autowired
    private lateinit var idGenerator: IdGenerator

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var mongoStore: MongoStore

    @Autowired
    private lateinit var deviceManager: DeviceManager

    @Autowired
    private lateinit var authManager: AuthManager

    private val log: Logger = LoggerFactory.getLogger(UserHandler::class.java)

    suspend fun register(request: RegisterRequest): RegisterResponse {
        log.info("收到注册请求:{}", requestProcessor.toJson(request))
        var user = User().apply {
            userId = idGenerator.userId(request.username)
            password = passwordEncoder.encode(request.password)
            createTime = LocalDateTime.now()
            registerDeviceId = StringUtils.defaultIfBlank(request.deviceId, UUID.randomUUID().toString())
            username = request.username
            avatar = "default_user_avatar"
            displayName = "im.joker:" + idGenerator.nextUserSequence()
        }
        try {
            user = mongoStore.addUser(user)
        } catch (e: Exception) {
            log.error("addUser报错啦", e)
            throw ImException(ErrorCode.M_USER_IN_USE, HttpStatus.FORBIDDEN)
        }
        val device = deviceManager.findOrCreateDevice(request.username, user.registerDeviceId, user.userId,
                request.initialDeviceDisplayName, user.avatar, user.displayName)

        return RegisterResponse().apply {
            accessToken = device.accessToken
            userId = user.userId
            deviceId = device.deviceId
        }
    }

    suspend fun login(request: LoginRequest): LoginResponse {
        requestProcessor.validate(request)
        return authManager.login(request)
    }

    suspend fun logout(device: Device) {
        authManager.logout(device)
    }

    suspend fun logoutAll(device: Device) {
        authManager.logoutAll(device)
    }

    suspend fun findUser(userId: String): User? {
        return mongoStore.findUserByUserId(userId)
    }


}