package im.joker.event.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 房间事件。
 *
 * @author linyurong
 **/
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class AbstractRoomEvent implements IRoomEvent {

    protected String roomId;

    protected String eventId;

    protected String type;

    protected Long originServerTs;

    protected String transactionId;

    protected UnsignedData unsigned;

    protected String sender;

    protected Long streamId;
}
