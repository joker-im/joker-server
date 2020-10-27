package im.joker.event.room;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public abstract class AbsStateEvent implements StateEvent {

    protected String stateKey;

    protected String roomId;

    protected String eventId;

    protected String type;

    protected Long originServerTs;

    protected String transactionId;

    protected UnsignedData unsignedData;

    protected String sender;

}
