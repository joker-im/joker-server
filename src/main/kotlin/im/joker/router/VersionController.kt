package im.joker.router

import im.joker.helper.ImCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/_matrix/client/versions"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController

class VersionController {

    @Autowired
    private lateinit var imCache: ImCache

    @GetMapping
    suspend fun version(): String {
        return """{"versions":["r0.6.0"],"unstable_features":{"org.matrix.label_based_filtering":true,"org.matrix.e2e_cross_signing":true,"org.matrix.msc2432":true,"uk.half-shot.msc2666":true,"io.element.e2ee_forced.public":false,"io.element.e2ee_forced.private":false,"io.element.e2ee_forced.trusted_private":false}}""";
    }

    @GetMapping("/test")
    suspend fun test(): Any {
        return imCache.getRoomState("!d6d569cd-778e-4169-a9d3-c767ea957613:www.jokerim.org")
    }
}