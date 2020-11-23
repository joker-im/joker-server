package im.joker.helper

import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.RoomJoinRuleType
import im.joker.event.content.state.*
import im.joker.event.room.state.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap

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
            this.stateKey = stateKey
            this.content = pc
            this.sender = sender
            streamId = idGenerator.nextEventStreamId()
            transactionId = UUID.randomUUID().toString()
            originServerTs = time
            this.roomId = roomId
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
        }
    }

    suspend fun roomJoinRuleEvent(joinRuleType: RoomJoinRuleType, roomId: String, sender: String, time: LocalDateTime): RoomJoinRuleEvent {
        val content = RoomJoinRuleContent().apply {
            this.joinRule = joinRuleType.name
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
        }
    }

}