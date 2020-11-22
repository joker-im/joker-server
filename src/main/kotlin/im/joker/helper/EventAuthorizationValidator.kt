package im.joker.helper

import im.joker.device.Device
import im.joker.event.room.AbstractRoomEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 22:20
 * @Desc:
 */
@Component
class EventAuthorizationValidator {

    val log: Logger = LoggerFactory.getLogger(EventAuthorizationValidator::class.java)


    /**
     * 检测是否可以发送此消息
     */
    suspend fun canPost(ev: AbstractRoomEvent, device: Device): Boolean {


    }

}