package im.joker.handler

import com.fasterxml.jackson.databind.JsonNode
import im.joker.api.vo.room.*
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.PresetType
import im.joker.event.RoomJoinRuleType
import im.joker.event.content.state.MembershipContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.other.FullReadMarkerEvent
import im.joker.event.room.other.ReceiptEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.*
import im.joker.repository.MongoStore
import im.joker.room.Room
import im.joker.room.RoomState
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 15:44
 * @Desc:
 */
@Component
class RoomHandler {

    val log: Logger = LoggerFactory.getLogger(RoomHandler::class.java)

    @Autowired
    private lateinit var imCache: ImCache

    @Autowired
    private lateinit var mongoStore: MongoStore

    @Autowired
    private lateinit var globalStateHolder: GlobalStateHolder

    @Autowired
    private lateinit var eventBuilder: ImEventBuilder

    @Autowired
    private lateinit var idGenerator: IdGenerator

    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    /**
     * 创建房间
     */
    suspend fun createRoom(device: Device, request: CreateRoomRequest): Room {
        val now = LocalDateTime.now()
        val that = this
        val room = Room().apply {
            createTime = now
            direct = request.direct ?: false
            creator = device.userId
            roomId = idGenerator.newRoomId()
            globalStateHolder = that.globalStateHolder
        }
        log.info("用户:{},创建一个新房间:{}", device.username, room.roomId)
        val createEvent = eventBuilder.createRoomEvent(device.userId, room.roomId, now)
        val joinEvent: MembershipEvent = eventBuilder.membershipEvent(room.roomId, now, device.userId,
                "", device.userId, device.name, device.userAvatar, MembershipType.Join)
        // 默认的权限定义事件
        val powerDefEvent = eventBuilder.powerDefEvent(room.roomId, device.userId, now)
        // 如果存在覆盖的
        request.powerLevelContentOverride?.let { powerDefEvent.content = it }
        //房间聊天记录是否可见
        val visibilityEvent = eventBuilder.defaultHistoryVisibilityEvent(room.roomId, device.userId, now)
        // 房间加入规则
        var joinRuleEvent = eventBuilder.roomJoinRuleEvent(RoomJoinRuleType.Public, room.roomId, device.userId, now)
        // 房间加入规则的预设状态不为空时
        PresetType.find(request.preset)?.let {
            if (this.equals(PresetType.Private_chat) || equals(PresetType.Trusted_private_chat)) {
                joinRuleEvent = eventBuilder.roomJoinRuleEvent(RoomJoinRuleType.Invite, room.roomId, device.userId, now)
            }
        }
        //是否存在顺带邀请用户
        val inviteUserEvents = request.invite?.map {
            eventBuilder.membershipEvent(room.roomId, now, device.userId,
                    "新房邀请", it, device.name, device.userAvatar, MembershipType.Invite)
        }

        // 初始事件
        val initRoomStateEvents = request.initialState?.map {
            it.eventId = UUID.randomUUID().toString()
            it.stateKey = ""
            it.streamId = idGenerator.nextEventStreamId()
            it.originServerTs = now
            it.transactionId = UUID.randomUUID().toString()
            it.roomId = room.roomId
            it.sender = device.userId
            it
        }

        //重命名事件
        val roomNameEvent = request.name?.let {
            eventBuilder.roomNameEvent(it, room.roomId, device.userId, now)
        }
        //topic事件
        val roomTopicEvent = request.topic?.let {
            eventBuilder.roomTopicEvent(it, room.roomId, device.userId, now)
        }

        val totalEvent =
                mutableListOf<AbstractRoomEvent>(createEvent, joinEvent, powerDefEvent, visibilityEvent, joinRuleEvent)
        inviteUserEvents?.forEach {
            totalEvent.add(it)
        }
        initRoomStateEvents?.forEach {
            totalEvent.add(it)
        }
        roomNameEvent?.let {
            totalEvent.add(it)
        }
        roomTopicEvent?.let {
            totalEvent.add(it)
        }
        mongoStore.addRoom(room)
        room.injectEvents(totalEvent, device)
        return room
    }


    /**
     * 查询state_key当前加入的所有房间(不包含历史)
     */
    suspend fun searchRelatedRoomFromDb(stateKey: String): List<String> {
        val membershipEventsMap =
                findSpecifiedEvents(EventType.Membership, stateKey)
                        .map { it as MembershipEvent }
                        .groupingBy { it.roomId + it.type + it.stateKey }
                        .reduce { _, acc, e -> if (acc.streamId > e.streamId) acc else e }
        val destRoomIds = ArrayList<String>()
        membershipEventsMap.forEach { (_, v) ->
            if (MembershipType.Join.`is`(v.content.membership) || MembershipType.Invite.`is`(v.content.membership)) {
                destRoomIds.add(v.roomId)
            }
        }
        return destRoomIds
    }


    /**
     * 找出所有房间中指定的时间
     */
    suspend fun findSpecifiedEvents(eventType: EventType, stateKey: String): List<AbstractRoomEvent> {
        return mongoStore.findSpecifiedTypeEvents(eventType, stateKey)
    }

    suspend fun findRoom(roomId: String): Room {
        val room = mongoStore.findRoom(roomId)
        room?.let {
            it.globalStateHolder = globalStateHolder
        }
        if (room == null) return Room()
        return room
    }

    /**
     *  loginDevice 邀请一个用户进房间
     */
    suspend fun inviteToRoom(roomId: String, inviteRequest: InviteRequest, loginDevice: Device) {
        val room = imCache.getRoom(roomId)
        val membershipEvent = eventBuilder.membershipEvent(roomId, LocalDateTime.now(), loginDevice.userId,
                "主动邀请", inviteRequest.userId, loginDevice.userDisplayName, loginDevice.userAvatar, MembershipType.Invite)
        room.injectEvent(membershipEvent, loginDevice)
    }

    /**
     * loginDevice 加入一个房间
     */
    suspend fun joinRoom(roomId: String, loginDevice: Device) {
        val room = imCache.getRoom(roomId)
        val membershipEvent = eventBuilder.membershipEvent(roomId, LocalDateTime.now(), loginDevice.userId, "主动加入",
                loginDevice.userId, loginDevice.userDisplayName, loginDevice.userAvatar, MembershipType.Join)
        room.injectEvent(membershipEvent, loginDevice)
    }


    /**
     * loginDevice 离开房间
     */
    suspend fun leaveRoom(roomId: String, loginDevice: Device) {
        val room = imCache.getRoom(roomId)
        val membershipEvent = eventBuilder.membershipEvent(roomId, LocalDateTime.now(), loginDevice.userId, "主动离开",
                loginDevice.userId, loginDevice.userDisplayName, loginDevice.userAvatar, MembershipType.Leave)
        room.injectEvent(membershipEvent, loginDevice)
    }

    /**
     * 将某人T出房间
     */
    suspend fun kick(roomId: String, kickRequest: KickRequest, loginDevice: Device) {
        val room = imCache.getRoom(roomId)
        val membershipEvent = eventBuilder.membershipEvent(roomId, LocalDateTime.now(), loginDevice.userId, kickRequest.reason,
                kickRequest.userId, loginDevice.userDisplayName, loginDevice.userAvatar, MembershipType.Leave)
        room.injectEvent(membershipEvent, loginDevice)
    }


    /**
     * 获取某房间的事件块
     */
    suspend fun findEventsChunk(messageRequest: MessageRequest, loginDevice: Device): MessageResponse {
        // 当用户在房间是join的情况才能拉,而且只能拉join之后的,不然不给拉
        val roomState = imCache.getRoomState(messageRequest.roomId)
        val latestMembershipType = roomState.latestMembershipType(loginDevice.userId)
        if (latestMembershipType == null || latestMembershipType != MembershipType.Join) throw ImException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "您无权限查询该房间的消息")
        // 该用户join的时候最新的那条streamId
        val userLatestJoinStreamId = roomState.latestMembershipEvent(loginDevice.userId)!!.streamId
        // 无论往前拉还是往后拉,from都不能比join的streamId小
        if (messageRequest.from < userLatestJoinStreamId) {
            throw ImException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, "您无权限查询该房间更早的消息")
        }
        // 一次最多只给拉100
        messageRequest.limit = min(messageRequest.limit, 100)

        if (messageRequest.dir == "b") {
            val backwardEvents = mongoStore.findBackwardEvents(messageRequest.roomId, userLatestJoinStreamId, messageRequest.from, messageRequest.limit)
            val retStart = if (backwardEvents.isEmpty()) userLatestJoinStreamId else backwardEvents.last().streamId - 1
            // 往后拉的时候应该是不需要状态消息的. 并且start的位置最多是到join为止
            return MessageResponse().apply {
                chunk = backwardEvents
                start = retStart
                end = userLatestJoinStreamId
            }

        } else {
            val forwardEvents = mongoStore.findForwardRoomEvents(messageRequest.roomId, messageRequest.from, messageRequest.limit)
            val latestStreamId = mongoStore.findLatestStreamId()
            val retStart = if (forwardEvents.isEmpty()) latestStreamId else forwardEvents.last().streamId + 1
            val stateEvents = forwardEvents.filterIsInstance<AbstractRoomStateEvent>()
            val handledStateEvents = RoomState.fromEvents(stateEvents).distinctStateEvents()
            return MessageResponse().apply {
                chunk = forwardEvents
                state = handledStateEvents
                start = retStart
                end = latestStreamId
            }
        }
    }

    suspend fun sendMessageEvent(type: String, jsonBody: String, roomId: String, txId: String, device: Device): EventIdResponse {
        val eventType = EventType.findByType(type)
        eventType ?: throw ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "无法识别次事件类型")
        if (eventType.isState) throw ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "此接口不支持状态事件")
        val contentObject = requestProcessor.toBean(jsonBody, JsonNode::class.java)
        val eventJsonObject = requestProcessor.createObjectNode()
        eventJsonObject.put("type", type)
        eventJsonObject.set<JsonNode>("content", contentObject)
        val event = requestProcessor.toBean(eventJsonObject.toString(), AbstractRoomEvent::class.java)
                .apply {
                    this.roomId = roomId
                    this.transactionId = txId
                    this.streamId = idGenerator.nextEventStreamId()
                    this.sender = device.userId
                    this.originServerTs = LocalDateTime.now()
                    this.eventId = UUID.randomUUID().toString()
                }

        val room = imCache.getRoom(roomId)
        room.injectEvent(event, device)
        return EventIdResponse().apply {
            this.eventId = event.eventId
        }
    }

    suspend fun setReadMarker(roomId: String, readMarkerRequest: ReadMarkerRequest, loginDevice: Device) {
        val now = LocalDateTime.now()
        var fullReadMarkerEvent: FullReadMarkerEvent? = null
        var receiptEvent: ReceiptEvent? = null
        readMarkerRequest.fullRead?.let {
            fullReadMarkerEvent = eventBuilder.fullReadMarkerEvent(roomId, readMarkerRequest.fullRead, now, loginDevice)
        }
        readMarkerRequest.read?.let {
            receiptEvent = eventBuilder.receiptEvent(roomId, readMarkerRequest.read, now, loginDevice)
        }
        val room = imCache.getRoom(roomId)
        listOf(fullReadMarkerEvent, receiptEvent).forEach {
            it?.let {
                room.injectEvent(it, loginDevice)
            }
        }

    }

    suspend fun findRoomMembers(findMembersRequest: FindMembersRequest): FindMembersResponse {
        val roomState = imCache.getRoomState(findMembersRequest.roomId)

        return FindMembersResponse().apply {
            findMembersRequest.membership?.let {
                this.chunk = roomState.findMembershipEvents(MembershipType.find(findMembersRequest.membership))
            }
        }

    }

    suspend fun sendTypingEvent(typingRequest: TypingRequest, loginDevice: Device) {
        if (typingRequest.typing) {
            val typingEvent = eventBuilder.typingEvent(loginDevice.userId, typingRequest.roomId, LocalDateTime.now())
            imCache.getRoom(typingEvent.roomId).injectEvent(typingEvent, loginDevice)
        }
    }


}