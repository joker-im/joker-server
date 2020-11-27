package im.joker.router

import im.joker.api.vo.presence.PresenceRequest
import im.joker.handler.PresenceHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PresenceController : BaseController() {


    @Autowired
    private lateinit var presenceHandler: PresenceHandler

    @PutMapping("/presence/{userId}/status")
    suspend fun setPresence(@PathVariable userId: String, @RequestBody presenceRequest: PresenceRequest): String {
        presenceHandler.setPresence(presenceRequest, getLoginDevice())
        return "{}"
    }

}