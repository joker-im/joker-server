package im.joker.router

import im.joker.api.vo.media.UploadResponse
import im.joker.handler.MediaHandler
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class MediaRouter : BaseRouter() {


    private lateinit var mediaHandler: MediaHandler

    @PostMapping("/upload")
    suspend fun upload(): UploadResponse {
        return mediaHandler.upload(getLoginDevice())
    }

}