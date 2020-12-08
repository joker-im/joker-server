package im.joker.controller

import im.joker.api.vo.media.ThumbnailRequest
import im.joker.api.vo.media.UploadResponse
import im.joker.handler.MediaHandler
import im.joker.helper.RequestProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.net.URLEncoder
import java.util.*
import kotlin.collections.HashMap

@RequestMapping(path = ["/_matrix/media/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class MediaController : BaseController() {

    private val defaultAvatarMap = HashMap<String, ByteArray>().apply {
        this["default_user_avatar"] = MediaController::class.java.getResourceAsStream("/avatar/default_user_avatar.png").readBytes()
        this["default_room_avatar"] = MediaController::class.java.getResourceAsStream("/avatar/default_room_avatar.png").readBytes()
    }


    @Autowired
    private lateinit var mediaHandler: MediaHandler

    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    @GetMapping("/_matrix/media/r0/config")
    suspend fun config(): Map<String, Int> {
        return mapOf("m.upload.size" to 209715200)
    }


    @PostMapping("/upload")
    suspend fun upload(@RequestBody body: ByteArray,
                       @RequestParam("filename", required = false) filename: String?,
                       @RequestHeader(HttpHeaders.CONTENT_TYPE) contentType: String?): UploadResponse {

        return mediaHandler.upload(getLoginDevice(), body, filename ?: UUID.randomUUID().toString(), contentType)

    }

    @GetMapping("/download/{serverName}/{mediaId}", produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE])
    suspend fun download(@PathVariable serverName: String, @PathVariable mediaId: String): ResponseEntity<ByteArray> {
        if (defaultAvatarMap[mediaId] != null) {
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentValue("$mediaId.png"))
                    .header(HttpHeaders.CONTENT_TYPE, "image/png")
                    .body(defaultAvatarMap[mediaId]!!)
        }
        val uploadFile = mediaHandler.getFile(mediaId)
        uploadFile ?: return ResponseEntity.notFound().build()
        return withContext(Dispatchers.IO) {
            val retFile = File(uploadFile.filePath)
            val builder = ResponseEntity.ok()
            uploadFile.contentType?.let {
                builder.header(HttpHeaders.CONTENT_TYPE, it)
            }
            return@withContext builder.header(HttpHeaders.CONTENT_DISPOSITION, getAttachmentValue(uploadFile.filename)).body(retFile.readBytes())
        }
    }


    @GetMapping("/thumbnail/{serverName}/{mediaId}")
    suspend fun downloadThumbnail(@PathVariable serverName: String,
                                  @PathVariable mediaId: String,
                                  @RequestParam param: Map<String, Any>): ResponseEntity<ByteArray> {
        return download(serverName, mediaId)
    }

    /**
     * 其实不需要返回文件名
     */
    private fun getAttachmentValue(filename: String): String {
        val urlFileName = URLEncoder.encode(filename, "UTF-8").replace("\\+".toRegex(), "%20")
        return "attachment;filename=\"$urlFileName\";filename*=utf-8''$urlFileName"
    }


}