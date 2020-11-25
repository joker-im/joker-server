package im.joker.handler

import im.joker.api.vo.media.UploadResponse
import im.joker.device.Device
import im.joker.repository.MongoStore
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class MediaHandler {

    val log: Logger = LoggerFactory.getLogger(MediaHandler::class.java)

    @Autowired
    private lateinit var mongoStore: MongoStore

    @Value("\${im.web-domain}")
    lateinit var webDomain: String


    suspend fun upload(device: Device): UploadResponse {
        var fileId = ""

        return UploadResponse().apply {
            contentUri = "mxc://$webDomain/$fileId"
        }
    }

}