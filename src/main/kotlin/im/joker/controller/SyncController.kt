package im.joker.controller

import im.joker.api.vo.sync.FilterResponse
import im.joker.api.vo.sync.SyncRequest
import im.joker.api.vo.sync.SyncResponse
import im.joker.handler.SyncHandler
import im.joker.helper.RequestProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class SyncController : BaseController() {

    @Autowired
    private lateinit var syncHandler: SyncHandler

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    @GetMapping("/sync")
    suspend fun sync(@RequestParam param: Map<String, String>): SyncResponse {
        return syncHandler.sync(requestProcessor.convert(param, SyncRequest::class.java), getLoginDevice())
    }

    @PostMapping("/user/{userId}/filter")
    suspend fun filter(@PathVariable userId: String): FilterResponse {
        return FilterResponse().apply {
            filterId = UUID.randomUUID().toString()
        }
    }

    @GetMapping("/user/{userId}/filter/{filterId}")
    suspend fun getFilter(@PathVariable userId: String, @PathVariable filterId: String): String {
        return """{"room":{"state":{"types":["m.room.*"],"not_rooms":["!726s6s6q:example.com"]},"timeline":{"limit":10,"types":["m.room.message"],"not_rooms":["!726s6s6q:example.com"],"not_senders":["@spam:example.com"]},"ephemeral":{"types":["m.receipt","m.typing"],"not_rooms":["!726s6s6q:example.com"],"not_senders":["@spam:example.com"]}},"presence":{"types":["m.presence"],"not_senders":["@alice:example.com"]},"event_format":"client","event_fields":["type","content","sender"]}""";
    }

}