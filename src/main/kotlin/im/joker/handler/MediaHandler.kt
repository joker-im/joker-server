package im.joker.handler

import im.joker.api.vo.media.UploadResponse
import im.joker.device.Device
import im.joker.repository.MongoStore
import im.joker.upload.UploadFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime

@Component
class MediaHandler {

    val log: Logger = LoggerFactory.getLogger(MediaHandler::class.java)

    @Autowired
    private lateinit var mongoStore: MongoStore

    @Value("\${im.web-domain}")
    lateinit var webDomain: String

    @Value("\${im.file-root-dir}")
    lateinit var fileDir: String


    suspend fun upload(device: Device, body: ByteArray, filename: String, contentType: String): UploadResponse {
        return withContext(Dispatchers.IO) {
            log.debug("上传的文件为:file:{}", filename)
            val dir = File(fileDir + "/" + LocalDate.now(), device.username)
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, RandomStringUtils.randomAlphabetic(4) + "_" + System.currentTimeMillis() + "_" + filename)
            file.writeBytes(body)
            val uploadFile = UploadFile().apply {
                this.filePath = file.absolutePath
                this.uploadTime = LocalDateTime.now()
                // 客户端不给名字就自己命名
                this.filename = filename
                this.username = device.username
                this.servername = webDomain
                this.contentType = contentType
            }
            mongoStore.addUploadFile(uploadFile)

            return@withContext UploadResponse().apply {
                contentUri = toMediaUrl(uploadFile.id)
            }
        }

    }

    suspend fun getFile(mediaId: String): UploadFile? {
        return mongoStore.findUploadFile(mediaId)

    }

    fun toMediaUrl(id: String): String {
        return "mxc://${webDomain}/${id}"
    }

}