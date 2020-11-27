package im.joker.router

import im.joker.config.AuthFilter
import im.joker.device.Device
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
open class BaseController {

    suspend fun getLoginDevice(): Device {
        return Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingle()
    }
}