package im.joker.router

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class VoipRouter {

    @GetMapping("/voip/turnServer")
    suspend fun queryTurnServers(): String {
        return """{"username":"","password":"","uris":[],"ttl":86400}"""
    }
}