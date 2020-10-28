package im.joker.event.room;

import lombok.Data;

@Data
public abstract class AbstractMessageEvent implements IRoomEvent {

    protected String roomId;

    protected String eventId;

    protected String type;

    protected Long originServerTs;

    protected String transactionId;

    protected UnsignedData unsigned;

    protected String sender;





}
