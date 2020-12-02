package im.joker.room

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document
class RoomReadMarker {

    @Id
    lateinit var id: String

    lateinit var userId: String

    lateinit var roomId: String

    lateinit var readMarkerTime: LocalDateTime

    lateinit var eventId: String

    var streamId: Long? = null


}