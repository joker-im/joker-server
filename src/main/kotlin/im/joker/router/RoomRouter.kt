package im.joker.router

import com.google.gson.JsonObject
import im.joker.api.vo.room.*
import im.joker.event.EventType
import im.joker.event.room.AbstractRoomEvent
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.RequestProcessor
import im.joker.handler.RoomHandler
import im.joker.helper.ImCache
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 21:50
 * @Desc:
 */
@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class RoomRouter : BaseRouter() {

    private val log: Logger = LoggerFactory.getLogger(RoomRouter::class.java)

    @Autowired
    private lateinit var roomHandler: RoomHandler

    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    @Autowired
    private lateinit var imCache: ImCache

    /**
     * 创建房间
     */
    @PostMapping("/createRoom")
    suspend fun createRoom(@RequestBody roomRequest: CreateRoomRequest): CreateRoomResponse {
        val ret = roomHandler.createRoom(getLoginDevice(), roomRequest)
        return CreateRoomResponse().apply {
            roomId = ret.roomId
        }
    }


    /**
     * 发送房间事件
     */
    @PutMapping("/rooms/{roomId}/send/{eventType}/{txnId}")
    suspend fun sendMessageEvent(@PathVariable roomId: String,
                                 @PathVariable("eventType") type: String,
                                 @PathVariable("txnId") txId: String,
                                 @RequestBody jsonBody: String): EventIdResponse {
        val eventType = EventType.findByType(type)
        eventType ?: throw ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "无法识别次事件类型")
        if (eventType.isState) throw ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "此接口不支持状态事件")
        val contentObject = requestProcessor.toBean(jsonBody, JsonObject::class.java)
        val eventJsonObject = JsonObject()
        eventJsonObject.addProperty("type", type)
        eventJsonObject.add("content", contentObject)
        val event = requestProcessor.toBean(eventJsonObject.toString(), AbstractRoomEvent::class.java)
        val room = imCache.getRoom(roomId)
        room.injectEvent(event, getLoginDevice())
        return EventIdResponse().apply {
            this.eventId = eventId
        }
    }


    @PostMapping("/rooms/{roomId}/invite")
    suspend fun inviteToRoom(@PathVariable roomId: String, @RequestBody inviteRequest: InviteRequest) {
        roomHandler.inviteToRoom(roomId, inviteRequest, getLoginDevice())
    }


    @PostMapping("/rooms/{roomId}/join")
    suspend fun joinRoom(@PathVariable roomId: String): JoinRoomResponse {
        roomHandler.joinRoom(roomId, getLoginDevice())
        return JoinRoomResponse().apply {
            this.roomId = roomId
        }
    }

    @PostMapping("/rooms/{roomId}/leave")
    suspend fun leaveRoom(@PathVariable roomId: String) {
        roomHandler.leaveRoom(roomId, getLoginDevice())
    }

    @PostMapping("/rooms/{roomId}/kick")
    suspend fun kick(@PathVariable roomId: String, @RequestBody kickRequest: KickRequest) {
        requestProcessor.validate(kickRequest)
        roomHandler.kick(roomId, kickRequest, getLoginDevice())
    }


    /**
     * 从此房间中拉取消息
     */
    @GetMapping("/rooms/{roomId}/message")
    suspend fun messages(@PathVariable roomId: String, @RequestBody param: Map<String, String>): MessageResponse {
        val messageRequest = requestProcessor.convert(param, MessageRequest::class.java)
        log.info("收到拉取房间消息的请求,deviceId:{},roomId:{}", getLoginDevice().deviceId, roomId)
        requestProcessor.validate(messageRequest)
        messageRequest.roomId = roomId
        return roomHandler.findEventsChunk(messageRequest, getLoginDevice())
    }


}