package im.joker.handler

import im.joker.api.vo.presence.PresenceRequest
import im.joker.constants.ImConstants.Companion.USER_PRESENCE
import im.joker.device.Device
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.RequestProcessor
import im.joker.helper.RoomSubscribeManager
import im.joker.presence.PresenceType
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 17:36
 * @Desc:
 */
@Component
class PresenceHandler {

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

    @Autowired
    private lateinit var roomSubscribeManager: RoomSubscribeManager

    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    private val log: Logger = LoggerFactory.getLogger(PresenceHandler::class.java)


    suspend fun setPresence(request: PresenceRequest, device: Device) {
        log.info("用户id:{},设备:{},正在登录,并设置状态:{},状态信息为:{}", device.userId, device.deviceId, request.presence, request.statusMsg)
        val presence = Arrays.stream(PresenceType.values())
                .filter { presenceType: PresenceType -> presenceType.id == request.presence }
                .findAny()
        if (presence.isEmpty) {
            throw ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST)
        }
        redisTemplate.opsForValue().set(USER_PRESENCE.format(device.deviceId), presence.get().id, Duration.ofDays(7)).awaitSingleOrNull()
        roomSubscribeManager.updateRelation(device, presence.get())
    }

}