package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.CreateContent;
import im.joker.event.room.AbstractRoomStateEvent;


public class RoomCreateEvent extends AbstractRoomStateEvent {


    private CreateContent content;


    @Override
    public IContent getContent() {
        return content;
    }

    public void setContent(CreateContent content) {
        this.content = content;
    }
}
