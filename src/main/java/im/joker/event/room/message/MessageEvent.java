package im.joker.event.room.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.IContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.IRoomEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent extends AbstractRoomEvent {

    private AbstractMessageContent content;

    @Override
    public IContent getContent() {
        return content;
    }

}
