package im.joker.upload

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


@Document
class UploadFile {

    @Id
    lateinit var id: String

    lateinit var filename: String

    lateinit var filePath: String

    lateinit var servername: String

    lateinit var username: String

    lateinit var contentType : String

    lateinit var uploadTime: LocalDateTime

}