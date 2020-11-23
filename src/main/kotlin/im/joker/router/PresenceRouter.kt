package im.joker.router

import im.joker.api.vo.presence.PresenceRequest
import im.joker.config.AuthFilter
import im.joker.device.Device
import im.joker.handler.PresenceHandler
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PresenceRouter {


    @Autowired
    private lateinit var presenceHandler: PresenceHandler

    @PutMapping("/presence/{userId}/status")
    suspend fun setPresence(@PathVariable userId: String, @RequestBody presenceRequest: PresenceRequest) {
        val device = Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingle()
        presenceHandler.setPresence(presenceRequest, device)
    }

}