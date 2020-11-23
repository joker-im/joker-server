package im.joker.router

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Component
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoipRouter {

    @GetMapping("/voip/turnServer")
    suspend fun queryTurnServers(): String {
        return """{"username":"1443779631:@user:example.com","password":"JlKfBy1QwLrO20385QyAtEyIv0=","uris":["turn:turn.example.com:3478?transport=udp","turn:10.20.30.40:3478?transport=tcp","turns:10.20.30.40:443?transport=tcp"],"ttl":86400}""";
    }
}