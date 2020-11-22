package im.joker.router

import im.joker.api.vo.room.CreateRoomRequest
import im.joker.api.vo.room.CreateRoomResponse
import im.joker.config.AuthFilter
import im.joker.device.Device
import im.joker.room.RoomManager
import kotlinx.coroutines.reactive.awaitSingle
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 21:50
 * @Desc:
 */
@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class RoomRouter {

    private val log: Logger = LoggerFactory.getLogger(RoomRouter::class.java)

    @Autowired
    private lateinit var roomManager: RoomManager

    @PostMapping("/createRoom")
    suspend fun createRoom(roomRequest: CreateRoomRequest): CreateRoomResponse {
        val device = Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingle()
        val ret = roomManager.createRoom(device, roomRequest)
    }
}