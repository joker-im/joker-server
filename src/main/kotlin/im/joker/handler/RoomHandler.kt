package im.joker.handler

import im.joker.api.vo.room.*
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.PresetType
import im.joker.event.RoomJoinRuleType
import im.joker.event.content.state.MembershipContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.GlobalStateHolder
import im.joker.helper.IdGenerator
import im.joker.helper.ImCache
import im.joker.helper.ImEventBuilder
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
import kotlin.math.max
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
    suspend fun searchJoinRoomIdsFromDb(stateKey: String): List<String> {
        val membershipEventsMap =
                findSpecifiedEvents(EventType.Membership, stateKey)
                        .map { it as AbstractRoomStateEvent }
                        .groupingBy { it.roomId + it.type + it.stateKey }
                        .reduce { _, acc, e -> if (acc.streamId > e.streamId) acc else e }
        val destRoomIds = ArrayList<String>()
        membershipEventsMap.forEach { (_, v) ->
            val content = v.content as MembershipContent
            if (MembershipType.Join.`is`(content.membership)) {
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
        val membershipEvent = eventBuilder.membershipEvent(roomId, LocalDateTime.now(), loginDevice.deviceId,
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
            // 往后拉的时候应该是不需要状态消息的. 并且start的位置最多是到join为止
            return MessageResponse().apply {
                chunk = backwardEvents
                start = max(backwardEvents.last().streamId - 1, userLatestJoinStreamId)
                end = userLatestJoinStreamId
            }

        } else {
            val forwardEvents = mongoStore.findForwardRoomEvents(messageRequest.roomId, messageRequest.from, messageRequest.limit)
            val stateEvents = forwardEvents.filterIsInstance<AbstractRoomStateEvent>()
            val handledStateEvents = RoomState.fromEvents(stateEvents).distinctStateEvents()
            return MessageResponse().apply {
                chunk = forwardEvents
                state = handledStateEvents
                start = forwardEvents.last().streamId + 1
                end = mongoStore.findLatestStreamId()
            }
        }
    }


}