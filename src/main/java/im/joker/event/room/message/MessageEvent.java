package im.joker.event.room.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import im.joker.event.content.AbstractMessageContent;
import im.joker.event.room.AbstractRoomEvent;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;

public class MessageEvent extends AbstractRoomEvent {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "msgtype", visible = true, include = EXISTING_PROPERTY)
    private AbstractMessageContent content;

    @Override
    public AbstractMessageContent getContent() {
        return content;
    }

    public void setContent(AbstractMessageContent content) {
        this.content = content;
    }
}
