package im.joker.event.room.message;

import im.joker.event.content.IContent;
import im.joker.event.content.message.RedactionContent;
import im.joker.event.room.AbstractRoomEvent;

public class RedactionEvent extends AbstractRoomEvent {


    private RedactionContent content;


    @Override
    public IContent getContent() {
        return content;
    }


    public void setContent(RedactionContent content) {
        this.content = content;
    }
}
