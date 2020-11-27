package im.joker.router

import com.fasterxml.jackson.databind.JsonNode
import im.joker.api.vo.room.*
import im.joker.event.EventType
import im.joker.event.room.AbstractRoomEvent
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.handler.RoomHandler
import im.joker.helper.ImCache
import im.joker.helper.RequestProcessor
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
class RoomController : BaseController() {

    private val log: Logger = LoggerFactory.getLogger(RoomController::class.java)

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
        return roomHandler.sendMessageEvent(type, jsonBody, roomId, txId, getLoginDevice())
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
    @GetMapping("/rooms/{roomId}/messages")
    suspend fun messages(@PathVariable roomId: String, @RequestParam param: Map<String, String>): MessageResponse {
        val messageRequest = requestProcessor.convert(param, MessageRequest::class.java)
        log.info("收到拉取房间消息的请求,deviceId:{},roomId:{}", getLoginDevice().deviceId, roomId)
        requestProcessor.validate(messageRequest)
        messageRequest.roomId = roomId
        return roomHandler.findEventsChunk(messageRequest, getLoginDevice())
    }


}