package im.joker.event.room;

import lombok.Data;

@Data
public abstract class AbsMessageEvent implements IRoomEvent {

    protected String roomId;

    protected String eventId;

    protected String type;

    protected Long originServerTs;

    protected String transactionId;

    protected UnsignedData unsignedData;

    protected String sender;





}
