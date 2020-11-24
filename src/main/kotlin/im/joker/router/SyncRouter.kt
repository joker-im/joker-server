package im.joker.router

import im.joker.api.vo.sync.SyncRequest
import im.joker.api.vo.sync.SyncResponse
import im.joker.config.AuthFilter
import im.joker.device.Device
import im.joker.handler.SyncHandler
import im.joker.helper.RequestProcessor
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class SyncRouter {

    @Autowired
    private lateinit var syncHandler: SyncHandler

    @Autowired
    private lateinit var requestProcessor: RequestProcessor


    @GetMapping("/sync")
    suspend fun sync(@RequestParam param: Map<String, String>): SyncResponse {
        val syncRequest = requestProcessor.convert(param, SyncRequest::class.java)
        val device = Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingle()
        return syncHandler.sync(syncRequest, device)
    }

}