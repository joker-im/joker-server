package im.joker.event.room.message;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.IContent;
import im.joker.event.room.AbstractRoomEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent extends AbstractRoomEvent {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "msgtype", visible = true)
    private AbstractMessageContent content;

    @Override
    public IContent getContent() {
        return content;
    }

}
