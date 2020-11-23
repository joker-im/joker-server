package im.joker.event.room.state;

import im.joker.event.content.state.RoomNameContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class RoomNameEvent extends AbstractRoomStateEvent {


    private RoomNameContent content;


    @Override
    public RoomNameContent getContent() {
        return content;
    }


    public void setContent(RoomNameContent content) {
        this.content = content;
    }
}
