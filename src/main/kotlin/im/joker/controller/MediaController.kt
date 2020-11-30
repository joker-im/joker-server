package im.joker.controller

import im.joker.api.vo.media.UploadResponse
import im.joker.handler.MediaHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.net.URLEncoder

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class MediaController : BaseController() {

    private val defaultAvatarMap = HashMap<String, ByteArray>().apply {
        this["default_user_avatar"] = MediaController::class.java.getResourceAsStream("/avatar/default_user_avatar.png").readBytes()
        this["default_room_avatar"] = MediaController::class.java.getResourceAsStream("/avatar/default_room_avatar.png").readBytes()
    }


    @Autowired
    private lateinit var mediaHandler: MediaHandler

    @PostMapping("/upload", consumes = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    suspend fun upload(@RequestBody body: ByteArray, @RequestParam("filename", required = false) filename: String?): UploadResponse {

        return mediaHandler.upload(getLoginDevice(), body, filename)

    }

    @GetMapping("/download/{serverName}/{mediaId}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    suspend fun download(@PathVariable serverName: String, @PathVariable mediaId: String): ResponseEntity<ByteArray> {
        if (defaultAvatarMap[mediaId] != null) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentValue("$mediaId.png")).body(defaultAvatarMap[mediaId]!!)
        }
        val uploadFile = mediaHandler.getFile(mediaId)
        uploadFile ?: return ResponseEntity.notFound().build()
        return withContext(Dispatchers.IO) {
            val retFile = File(uploadFile.filePath)
            return@withContext ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentValue(uploadFile.filename)).body(retFile.readBytes());
        }
    }

    /**
     * 其实不需要返回文件名
     */
    private fun getAttachmentValue(filename: String): String {
        val urlFileName = URLEncoder.encode(filename, "UTF-8").replace("\\+".toRegex(), "%20")
        return "attachment;filename=\"$urlFileName\";filename*=utf-8''$urlFileName"
    }


}