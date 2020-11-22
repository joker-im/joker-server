package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.GuestAccessContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class GuestAccessEvent extends AbstractRoomStateEvent {


    private GuestAccessContent content;


    @Override
    public IContent getContent() {
        return content;
    }

    public void setContent(GuestAccessContent content) {
        this.content = content;
    }
}
