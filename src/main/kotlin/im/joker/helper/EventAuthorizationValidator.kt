package im.joker.helper

import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.content.state.MembershipContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.room.RoomState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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

    @Autowired
    private lateinit var imCache: ImCache

    /**
     * 检测是否可以发送此消息
     */
    suspend fun canPost(ev: AbstractRoomEvent, device: Device): Boolean {
        log.debug("发送的事件类型为:{},roomId:{} ,sender:{},检测stateKey为:{}", ev.type, ev.roomId, ev.sender)
        val postEventType = EventType.findByType(ev.type)
        // 如果发送的消息为不支持的类型,那么直接false
        postEventType ?: return false
        val roomState = imCache.getRoomState(ev.roomId)
        val senderCurrentMembership = roomState.latestMembershipType(ev.sender)
        // 发送消息的人在房间中没有任何的membershipType,那么直接false
        senderCurrentMembership ?: return false

        when (senderCurrentMembership) {
            // 发送消息的人在此room的状态是invite.只能发送leave或者是join
            MembershipType.Invite -> {
                if (ev !is MembershipEvent) return false
                if (!MembershipType.Leave.`is`(ev.content.membership) || !MembershipType.Join.`is`(ev.content.membership)) return false
                return true
            }
            // 发送消息的人在此房间的状态是join,那么要判断各种发送事件权限细节
            MembershipType.Join -> {
                return canPostMembershipEvent(roomState, ev, device)
            }
            // sender在状态为leave时, 不给发
            else -> return false
        }

    }

    private fun canPostMembershipEvent(roomState: RoomState, senderEvent: AbstractRoomEvent, device: Device): Boolean {
        val powerDefContent = roomState.lastPowerDefEvent().content
        // 用户的权限值,如果有覆盖式的就用覆盖式,没有就默认
        val userPower = powerDefContent.users?.get(senderEvent.sender) ?: powerDefContent.userDefault
        when (EventType.findByType(senderEvent.type)) {
            // 当要发送的消息为聊天消息
            EventType.RoomMessage -> {
                val needPower = powerDefContent.events?.get(senderEvent.type) ?: powerDefContent.userDefault
                return userPower >= needPower
            }
            // 当发送的消息为membership消息
            EventType.Membership -> {
                val membershipType = (senderEvent.content as MembershipContent).membership
                when (MembershipType.find(membershipType)) {
                    MembershipType.Ban -> {
                        val needPower = powerDefContent.events?.get(membershipType) ?: (powerDefContent.ban
                                ?: powerDefContent.stateDefault)
                        return userPower >= needPower
                    }
                    MembershipType.Leave -> {
                        // 如果是自己想离开,那么随便走,但是是t的就要判断其权限
                        if ((senderEvent as MembershipEvent).stateKey == device.userId) {
                            return true
                        }
                        val needPower = powerDefContent.events?.get(membershipType) ?: (powerDefContent.kick
                                ?: powerDefContent.stateDefault)
                        return userPower >= needPower
                    }
                    MembershipType.Invite -> {
                        val needPower = powerDefContent.events?.get(membershipType) ?: (powerDefContent.invite
                                ?: powerDefContent.stateDefault)
                        return userPower >= needPower
                    }
                    else -> return false
                }
            }
            //当发送的是编辑事件
            EventType.Redaction -> {
                val needPower = powerDefContent.events?.get(senderEvent.type) ?: powerDefContent.redact
                return userPower >= needPower
            }
            EventType.MFullRead -> {
                return true
            }

            EventType.Receipt -> {
                return true
            }

            EventType.Typing -> {
                return true
            }

            // 暂时不支持其他事件
            else -> return false
        }
    }

}