package im.joker.handler

import im.joker.api.vo.sync.SyncRequest
import im.joker.api.vo.sync.SyncResponse
import im.joker.device.Device
import im.joker.event.MembershipType
import im.joker.helper.EventSyncQueueManager
import im.joker.helper.LongPollingHelper
import im.joker.helper.RoomSubscribeManager
import im.joker.repository.MongoStore
import im.joker.room.RoomState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

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

    private val limitOfRoom = 30


    suspend fun sync(request: SyncRequest, device: Device): SyncResponse {
        if (request.timeout == null || request.timeout < 5000) request.timeout = 30000
        val init = request.fullState ?: false || request.since == null
        return if (init) initSync(device) else incrSync(request, device)
    }

    private suspend fun incrSync(request: SyncRequest, device: Device): SyncResponse = coroutineScope {
        log.debug("userId:{},deviceId:{},触发增量同步", device.userId, device.deviceId)
        val ret = SyncResponse()
        val sinceId = request.since.toLong()
        val joinedMap = HashMap<String, SyncResponse.JoinedRooms>()
        val invitedMap = HashMap<String, SyncResponse.InvitedRooms>()
        val leftMap = HashMap<String, SyncResponse.LeftRooms>()
        // 查询最新的streamId
        val latestStreamId = mongodbStore.findLatestStreamId()

        ret.apply {
            this.rooms = SyncResponse.Rooms().apply {
                this.join = joinedMap
                this.invite = invitedMap
                this.leave = leftMap
            }
            nextBatch = (latestStreamId + 1).toString()
        }

        // 从感兴趣的房间里面拿到最新的消息
        val t2 = async { eventSyncQueueManager.takeRelatedEvent(device.deviceId, limitOfRoom, sinceId, latestStreamId) }
        val latestRoomEvents = t2.await()
        // 为空的时候,waiting timeout
        if (latestRoomEvents.isEmpty()) {
            val channel = Channel<Boolean>()
            longPollingHelper.addWaitingDevice(device.deviceId, channel)
            withTimeout(request.timeout.toLong()) {
                channel.receive()
            }
            return@coroutineScope ret
        }

        latestRoomEvents.forEach { roomId, roomEevents ->

        }

        ret
    }

    private suspend fun initSync(device: Device): SyncResponse = coroutineScope {
        log.info("userId:{},deviceId:{},触发全量同步event", device.userId, device.deviceId)
        // 查询最新的streamId
        val t1 = async { mongodbStore.findLatestStreamId() }
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
            //  state = ( currentFullRoomStateEvents#streamId < topK最老的一条streamId)
            //  timeline = topK
            val currentFullRoomStateEvents = latestRoomStateEvents.getValue(topK.roomId)
            val roomState = RoomState.fromEvents(currentFullRoomStateEvents.filter { it.streamId < topK.sliceLastEvents.last().streamId })
            // 判断同步的这个人在此房间处于什么状态
            when (roomState.latestMembershipType(device.userId)) {

                MembershipType.Invite -> {
                    val invited = SyncResponse.InvitedRooms().apply {
                        this.inviteState = SyncResponse.State().apply {
                            events = roomState.descStateEvent.sortedBy { it.streamId }
                        }
                    }
                    invitedMap[topK.roomId] = invited
                }
                MembershipType.Join -> {
                    // topK - lastStateEvent(MaxStreamId)
                    val joined = SyncResponse.JoinedRooms().apply {
                        this.timeline = SyncResponse.Timeline().apply {
                            limited = true
                            events = topK.sliceLastEvents.filter { it.streamId > latestRoomStateEvents.getValue(topK.roomId).first().streamId }
                        }
                        this.state = SyncResponse.State().apply {
                            events = RoomState.fromEvents(currentFullRoomStateEvents).distinctStateEvents()
                        }
                    }
                    joinedMap[topK.roomId] = joined
                }

                MembershipType.Leave -> {
                    // topK - lastStateEvent(MaxStreamId)
                    val left = SyncResponse.LeftRooms().apply {
                        this.timeline = SyncResponse.Timeline().apply {
                            events = topK.sliceLastEvents.filter { it.streamId > currentFullRoomStateEvents.first().streamId }
                            limited = true
                        }
                        this.state = SyncResponse.State().apply {
                            events = RoomState.fromEvents(currentFullRoomStateEvents).distinctStateEvents()
                        }
                    }
                    leftMap[topK.roomId] = left
                }
                // 这种情况是队列中和数据库中保持一致,只发生在房间事件很少的时候,全部放timeline
                else -> {
                    val joined = SyncResponse.JoinedRooms().apply {
                        this.timeline = SyncResponse.Timeline().apply {
                            limited = false
                            events = topK.sliceLastEvents.sortedBy { it.streamId }
                        }
                    }
                    joinedMap[topK.roomId] = joined
                }
            }
        }
        return@coroutineScope ret
    }

}