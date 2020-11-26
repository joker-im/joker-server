package im.joker.router

import im.joker.api.vo.sync.FilterResponse
import im.joker.api.vo.sync.SyncRequest
import im.joker.api.vo.sync.SyncResponse
import im.joker.config.AuthFilter
import im.joker.device.Device
import im.joker.handler.SyncHandler
import im.joker.helper.RequestProcessor
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono
import java.util.*

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class SyncRouter : BaseRouter() {

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

}