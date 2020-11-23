package im.joker.room

import im.joker.api.vo.room.CreateRoomRequest
import im.joker.device.Device
import im.joker.event.EventType
import im.joker.event.MembershipType
import im.joker.event.PresetType
import im.joker.event.RoomJoinRuleType
import im.joker.event.content.state.MembershipContent
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.AbstractRoomStateEvent
import im.joker.event.room.state.MembershipEvent
import im.joker.helper.GlobalStateHolder
import im.joker.helper.IdGenerator
import im.joker.helper.ImEventBuilder
import im.joker.repository.MongoStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.ReactiveStringRedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 15:44
 * @Desc:
 */
@Component
class RoomManager {

    val log: Logger = LoggerFactory.getLogger(RoomManager::class.java)

    @Autowired
    private lateinit var redisTemplate: ReactiveStringRedisTemplate

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
                "", device.userId, device.name ?: "", device.deviceAvatar ?: "", MembershipType.Join)
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
                    "", it, device.name ?: "", device.deviceAvatar ?: "", MembershipType.Invite)
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
                        .groupingBy { it.type + it.stateKey }
                        .reduce { _, acc, e -> if (acc.streamId > e.streamId) acc else e }
        val destRoomIds = ArrayList<String>()
        membershipEventsMap.forEach { (k, v) ->
            val content = v.content as MembershipContent
            if (MembershipType.Join.`is`(content.membership)) {
                destRoomIds.add(k)
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


}