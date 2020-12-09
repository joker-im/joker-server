package im.joker.helper

import im.joker.api.vo.room.TypingRequest
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.RoomJoinRuleType
import im.joker.event.content.other.FullReadContent
import im.joker.event.content.other.ReceiptContent
import im.joker.event.content.other.TypingContent
import im.joker.event.content.state.*
import im.joker.event.room.other.ReceiptEvent
import im.joker.event.room.other.TypingEvent
import im.joker.event.room.state.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class ImEventBuilder {

    @Autowired
    lateinit var idGenerator: IdGenerator


    suspend fun createRoomEvent(creator: String, roomId: String, createTime: LocalDateTime): RoomCreateEvent {

        val createContent = CreateContent().apply {
            this.creator = creator
            roomVersion = idGenerator.roomVersion()
            mFederate = false
        }
        return RoomCreateEvent().apply {
            content = createContent
            sender = creator
            streamId = idGenerator.nextEventStreamId()
            stateKey = ""
            type = EventType.Creation.id
            transactionId = UUID.randomUUID().toString()
            originServerTs = createTime
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }

    }

    suspend fun membershipEvent(roomId: String, time: LocalDateTime, sender: String,
                                reason: String, stateKey: String, displayName: String,
                                avatarUrl: String, membership: MembershipType): MembershipEvent {
        val membershipContent = MembershipContent().apply {
            this.avatarUrl = avatarUrl
            this.reason = reason
            this.membership = membership.id
            this.displayName = displayName
        }
        return MembershipEvent().apply {
            this.type = EventType.Membership.id
            this.stateKey = stateKey
            content = membershipContent
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }

    }

    suspend fun powerDefEvent(roomId: String, creator: String, now: LocalDateTime): PowerLevelEvent {
        val users = mapOf(creator to 100)
        return powerLevelEvent(60, 50, 60, 50, 50, null,
                50, 0, 50, users, roomId, creator, now)
    }

    /**
     * 房间权限定义事件生成
     *
     * @param ban
     * @param invite
     * @param kick
     * @param redact
     * @param state
     * @param events
     * @param message
     * @param room
     * @param users
     * @param roomId
     * @param sender
     * @param time
     * @return
     */
    suspend fun powerLevelEvent(ban: Int, invite: Int, kick: Int, redact: Int, state: Int, events: Map<String, Int>?, userDefault: Int,
                                message: Int, room: Int, users: Map<String, Int>, roomId: String?, sender: String,
                                time: LocalDateTime): PowerLevelEvent {
        val pc: PowerLevelContent = PowerLevelContent().apply {
            this.ban = ban
            this.invite = invite
            this.kick = kick
            this.events = events
            this.redact = redact
            this.stateDefault = state
            this.eventDefault = message
            this.users = users
            this.userDefault = userDefault
            this.notifications = PowerLevelContent.RoomNotificationPower().apply { this.room = room }
        }

        return PowerLevelEvent().apply {
            this.type = EventType.PowerLevel.id
            this.stateKey = ""
            this.content = pc
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }

    }

    /**
     * 创房时默认的房间历史是否可以查看事件
     *
     * @param roomId
     * @param sender
     * @return
     */
    suspend fun defaultHistoryVisibilityEvent(roomId: String?, sender: String?, time: LocalDateTime): HistoryVisibilityEvent {
        val hvc: HistoryVisibilityContent = HistoryVisibilityContent().apply {
            historyVisibility = "joined"
        }
        return HistoryVisibilityEvent().apply {
            stateKey = ""
            type = EventType.HistoryVisibility.id
            content = hvc
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }
    }

    suspend fun roomJoinRuleEvent(joinRuleType: RoomJoinRuleType, roomId: String, sender: String, time: LocalDateTime): RoomJoinRuleEvent {
        val content = RoomJoinRuleContent().apply {
            this.joinRule = joinRuleType.id
        }
        return RoomJoinRuleEvent().apply {
            stateKey = ""
            type = EventType.RoomJoinRule.id
            this.content = content
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }
    }

    suspend fun roomNameEvent(roomName: String, roomId: String, sender: String, time: LocalDateTime): RoomNameEvent {
        val roomNameContent: RoomNameContent = RoomNameContent().apply {
            name = roomName
        }
        return RoomNameEvent().apply {
            stateKey = ""
            type = EventType.RoomName.id
            content = roomNameContent
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }
    }

    suspend fun roomTopicEvent(topic: String, roomId: String, sender: String, time: LocalDateTime): RoomTopicEvent {
        val topicContent: RoomTopicContent = RoomTopicContent().apply {
            this.topic = topic
        }
        return RoomTopicEvent().apply {
            content = topicContent
            stateKey = ""
            type = EventType.RoomTopic.id
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
            eventId = UUID.randomUUID().toString()
        }
    }


    suspend fun receiptEvent(roomId: String, read: String, now: LocalDateTime, loginDevice: Device): ReceiptEvent {
        val eventId = UUID.randomUUID().toString()
        val content = ReceiptContent().apply {
            properties[eventId] = ReceiptContent.Receipts().apply {
                val receipt = ReceiptContent.Receipt().apply {
                    this.ts = now
                }
                this.read[loginDevice.userId] = receipt
            }
        }
        return ReceiptEvent().apply {
            this.content = content
            this.roomId = roomId
            type = EventType.Receipt.id
            this.sender = loginDevice.userId
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = now
            this.eventId = UUID.randomUUID().toString()

        }
    }

    suspend fun typingEvent(sender: String, typingRequest: TypingRequest, time: LocalDateTime): TypingEvent {
        val typingContent = TypingContent().apply {
            this.timeout = typingRequest.timeout
            this.typing = typingRequest.typing
        }
        return TypingEvent().apply {
            this.content = typingContent
            this.roomId = typingRequest.roomId
            type = EventType.Typing.id
            this.sender = sender
            originServerTs = time
        }
    }

    fun roomAvatarEvent(): RoomAvatarEvent? {
        return null
    }

}