package im.joker.handler

import im.joker.api.vo.sync.SyncRequest
import im.joker.api.vo.sync.SyncResponse
import im.joker.device.Device
import im.joker.event.MembershipType
import im.joker.event.room.AbstractRoomEvent
import im.joker.event.room.UnsignedData
import im.joker.event.room.other.FullReadMarkerEvent
import im.joker.event.room.other.ReceiptEvent
import im.joker.event.room.other.TypingEvent
import im.joker.helper.*
import im.joker.repository.MongoStore
import im.joker.room.RoomState
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SyncHandler {


    private val log: Logger = LoggerFactory.getLogger(SyncHandler::class.java)

    @Autowired
    private lateinit var longPollingHelper: LongPollingHelper

    @Autowired
    private lateinit var eventSyncQueueManager: EventSyncQueueManager

    @Autowired
    private lateinit var mongodbStore: MongoStore

    @Autowired
    private lateinit var roomSubscribeManager: RoomSubscribeManager

    @Autowired
    private lateinit var roomMessageHelper: RoomMessageHelper

    @Autowired
    private lateinit var imCache: ImCache

    @Autowired
    private lateinit var idGenerator: IdGenerator

    private val limitOfRoom = 30


    suspend fun sync(request: SyncRequest, device: Device): SyncResponse {
        if (request.timeout == null || request.timeout < 5000) request.timeout = 30000
        val init = request.fullState ?: false || request.since == null
        return if (init) initSync(device) else incrementSync(request, device)
    }

    private suspend fun incrementSync(request: SyncRequest, device: Device, channel: Channel<Boolean> = Channel()): SyncResponse = coroutineScope {
        log.debug("userId:{},deviceId:{},触发增量同步", device.userId, device.deviceId)
        longPollingHelper.addWaitingDevice(device.deviceId, channel)
        val ret = SyncResponse()
        val sinceId = request.since.toLong()
        val joinedMap = HashMap<String, SyncResponse.JoinedRooms>()
        val invitedMap = HashMap<String, SyncResponse.InvitedRooms>()
        val leftMap = HashMap<String, SyncResponse.LeftRooms>()
        // 查询最新的streamId
        val latestStreamId = idGenerator.findLatestStreamId()

        ret.apply {
            this.rooms = SyncResponse.Rooms().apply {
                this.join = joinedMap
                this.invite = invitedMap
                this.leave = leftMap
            }
            nextBatch = (latestStreamId + 1).toString()
        }

        // 从感兴趣的房间里面拿到最新的消息
        val latestRoomEventMap = eventSyncQueueManager.takeRelatedEvent(device.deviceId, device.userId, sinceId, latestStreamId)
                .filter { entry ->
                    val roomId = entry.key
                    val events = entry.value
                    var latestRoomEvents = events
                    // 该用户在该房间能读的最大的streamId
                    val maxStreamId = roomMessageHelper.getRoomMaxStreamId(device.userId, roomId)
                    // 保证该用户只能sync到maxStreamId之间的事件
                    maxStreamId?.let { max ->
                        // 最多能读到这么多条数据
                        latestRoomEvents = events.filter { it.streamId <= max }
                    }
                    // 如果过滤后房间事件为0,那么跳过本房间处理
                    latestRoomEvents.isNotEmpty()
                }

        // 为空的时候,waiting timeout
        if (latestRoomEventMap.isEmpty()) {
            return@coroutineScope waitEvent(request, channel, ret, device)
        }
        // 不为空的时候,每个房间都要补充消息,不过需要先拿到当前的roomState,用于判断device所属的membership
        latestRoomEventMap.forEach { (roomId, events) ->
            var latestRoomEvents = events
            val fullRoomState = imCache.getRoomState(roomId)
            val queueMinStreamId = latestRoomEvents.first().streamId

            val startOfTimelineState = RoomState.fromEvents(fullRoomState.descStateEvent.filter { it.streamId < queueMinStreamId })
            var limited = false
            // 队列里面最小的streamId,但是最多只取limitOfRoom条
            if (latestRoomEvents.size > limitOfRoom) {
                latestRoomEvents = latestRoomEvents.slice(IntRange(latestRoomEvents.size - 1 - limitOfRoom, latestRoomEvents.size - 1))
                // 比队列里面最小的streamId还要小的作为timelineOfStartState
                limited = true
            }
            fillRetEvents(latestStreamId, fullRoomState, device, startOfTimelineState, invitedMap, roomId, latestRoomEvents, joinedMap, leftMap, limited)
        }

        ret
    }

    private suspend fun waitEvent(request: SyncRequest, channel: Channel<Boolean>, syncResponse: SyncResponse, device: Device): SyncResponse {
        var ret = syncResponse
        try {
            withTimeout(request.timeout.toLong()) {
                channel.receive()
                ret = incrementSync(request, device, channel)
            }
        } catch (e: Exception) {
            channel.cancel()
        }
        longPollingHelper.removeWaitingDevice(device.deviceId)
        return ret
    }

    private suspend fun initSync(device: Device): SyncResponse = coroutineScope {
        log.info("userId:{},deviceId:{},触发全量同步event", device.userId, device.deviceId)
        // 查询最新的streamId
        val t1 = async { idGenerator.findLatestStreamId() }
        // 拿取感兴趣的房间
        val t2 = async { roomSubscribeManager.searchJoinRoomIds(device.deviceId) }
        val latestStreamId = t1.await()
        val joinRoomIds = t2.await()
        // 查询每个房间的最新状态消息
        val t4 = async { mongodbStore.findRoomStateEvents(joinRoomIds, latestStreamId).sortedBy { it.streamId } }
        // 根据这些房间,查询其topK的消息,便于放入timeline
        val t5 = async { mongodbStore.findEventGroupByRoomTopK(joinRoomIds, limitOfRoom, latestStreamId) }
        val latestRoomStateEvents = t4.await().groupBy { it.roomId }
        val roomTopKEvents = t5.await()

        val ret = SyncResponse()
        val joinedMap = HashMap<String, SyncResponse.JoinedRooms>()
        val invitedMap = HashMap<String, SyncResponse.InvitedRooms>()
        val leftMap = HashMap<String, SyncResponse.LeftRooms>()
        ret.apply {
            this.rooms = SyncResponse.Rooms().apply {
                this.join = joinedMap
                this.invite = invitedMap
                this.leave = leftMap
            }
            nextBatch = (latestStreamId + 1).toString()
        }
        roomTopKEvents.forEach { topK ->
            // 该用户在该房间能读的最大的streamId
            val maxStreamId = roomMessageHelper.getRoomMaxStreamId(device.userId, topK.roomId)
            maxStreamId?.let { max ->
                topK.sliceLastEvents = topK.sliceLastEvents.filter { it.streamId < max }
            }
            if (topK.sliceLastEvents.isEmpty()) {
                return@forEach
            }
            //  timelineOfStartState = ( fullRoomStateEvents#streamId < topK最老的一条streamId)
            //  timeline = topK - timelineOfStartState 最新的一条状态消息
            // 过滤掉比topK最旧的streamId还要大的stateEvents即为 timelineOfStartState
            val topKMinStreamId = topK.sliceLastEvents.last().streamId
            val startOfTimelineState = RoomState.fromEvents(latestRoomStateEvents.getValue(topK.roomId).filter { it.streamId < topKMinStreamId })
            val timelineEvent = topK.sliceLastEvents.sortedBy { it.streamId }
            val fullRoomState = imCache.getRoomState(topK.roomId)
            // 填充事件到结果中
            fillRetEvents(latestStreamId, fullRoomState, device, startOfTimelineState, invitedMap, topK.roomId, timelineEvent, joinedMap, leftMap)
        }
        ret
    }

    private fun fillRetEvents(latestStreamId: Long, fullRoomState: RoomState, device: Device, startOfTimelineState: RoomState, invitedMap: HashMap<String, SyncResponse.InvitedRooms>, roomId: String,
                              timeline: List<AbstractRoomEvent>, joinedMap: HashMap<String, SyncResponse.JoinedRooms>, leftMap:
                              HashMap<String, SyncResponse.LeftRooms>, limited: Boolean = true) {

        val now = LocalDateTime.now()
        val ephemeralEvents = ephemeralEventHandle(timeline, now, device)

        val timelineEvent = timeline
                .filter { it !is TypingEvent && it !is ReceiptEvent && it !is FullReadMarkerEvent }
                .onEach {
                    it.unsigned = UnsignedData().apply {
                        age = ImTools.toMill(now) - ImTools.toMill(it.originServerTs)
                        transactionId = it.transactionId
                    }
                }

        val readMarkerEvent = timeline.filterIsInstance<FullReadMarkerEvent>()
        // 加这个为了防止startOfTimelineState数据可能是不存在的
        var startOfTimelineStateEvent = startOfTimelineState.distinctStateEvents()
        if (startOfTimelineStateEvent.isEmpty()) {
            startOfTimelineStateEvent = RoomState.fromEvents(timelineEvent).distinctStateEvents()
        }

        // 判断同步的这个人在此房间处于什么状态
        when (fullRoomState.latestMembershipType(device.userId)) {

            MembershipType.Invite -> {
                val invited = SyncResponse.InvitedRooms().apply {
                    this.inviteState = SyncResponse.State().apply {
                        events = startOfTimelineStateEvent
                    }
                }
                invitedMap[roomId] = invited
            }
            MembershipType.Join -> {
                val joined = SyncResponse.JoinedRooms().apply {
                    this.timeline = SyncResponse.Timeline().apply {
                        this.limited = limited
                        events = timelineEvent
                    }
                    if (timelineEvent.isNotEmpty()) {
                        this.timeline.prevBatch = (timelineEvent.last().streamId + 1).toString()
                    } else {
                        this.timeline.prevBatch = latestStreamId.toString()
                    }
                    if (limited) {
                        this.state = SyncResponse.State().apply {
                            events = startOfTimelineStateEvent
                        }
                    }
                    this.summary = SyncResponse.RoomSummary().apply {
                        val joinMembers = fullRoomState.findSpecificStateMembers(MembershipType.Join).take(4)
                        this.heroes = joinMembers
                        this.joinedMemberCount = joinMembers.size
                        this.invitedMemberCount = fullRoomState.findSpecificStateMembers(MembershipType.Invite).size
                    }
                    this.ephemeral = SyncResponse.Ephemeral().apply {
                        this.events = ephemeralEvents
                    }
                    this.accountData = SyncResponse.AccountData().apply {
                        this.events = readMarkerEvent
                    }
                }
                joinedMap[roomId] = joined
            }

            MembershipType.Leave -> {
                // topK - lastStateEvent(MaxStreamId)
                val left = SyncResponse.LeftRooms().apply {
                    this.timeline = SyncResponse.Timeline().apply {
                        events = timelineEvent
                        this.limited = limited
                    }

                    if (timelineEvent.isNotEmpty()) {
                        this.timeline.prevBatch = (timelineEvent.last().streamId + 1).toString()
                    } else {
                        this.timeline.prevBatch = latestStreamId.toString()
                    }

                    if (limited) {
                        this.state = SyncResponse.State().apply {
                            events = startOfTimelineStateEvent
                        }
                    }
                    this.accountData = SyncResponse.AccountData().apply {
                        this.events = readMarkerEvent
                    }
                }
                leftMap[roomId] = left
            }
            else -> return
        }
    }

    private fun ephemeralEventHandle(timeline: List<AbstractRoomEvent>, now: LocalDateTime, device: Device): List<TypingEvent> {
        var ephemeralEvents = timeline.filterIsInstance<TypingEvent>()
        if (ephemeralEvents.isNotEmpty()) {
            val destEphemeral = ephemeralEvents.first()
            ephemeralEvents.forEach {
                if (!typing(it, now)) {
                    destEphemeral.content.userIds.removeAll(it.content.userIds)
                } else {
                    destEphemeral.content.userIds.addAll(it.content.userIds)
                }
            }
            destEphemeral.content.userIds.remove(device.userId)
            ephemeralEvents = listOf(destEphemeral)
        }
        return ephemeralEvents
    }

    private fun typing(event: TypingEvent, now: LocalDateTime): Boolean {
        return event.content.typing &&
                ImTools.toMill(event.originServerTs) + event.content.timeout < ImTools.toMill(now)
    }

}