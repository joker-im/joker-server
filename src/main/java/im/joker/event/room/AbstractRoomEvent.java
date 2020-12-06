package im.joker.event.room;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;

/**
 * 房间事件。
 *
 * @author linyurong
 **/
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = EXISTING_PROPERTY, property = "type", visible = true)
public abstract class AbstractRoomEvent implements IRoomEvent {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String roomId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String eventId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected LocalDateTime originServerTs;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String transactionId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected UnsignedData unsigned;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected String sender;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Long streamId;

    @Override
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public LocalDateTime getOriginServerTs() {
        return originServerTs;
    }

    public void setOriginServerTs(LocalDateTime originServerTs) {
        this.originServerTs = originServerTs;
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public UnsignedData getUnsigned() {
        return unsigned;
    }

    public void setUnsigned(UnsignedData unsigned) {
        this.unsigned = unsigned;
    }

    @Override
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public Long getStreamId() {
        return streamId;
    }

    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }
}


