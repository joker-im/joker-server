package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomAvatarContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class RoomAvatarEvent extends AbstractRoomStateEvent {


    private RoomAvatarContent content;


    @Override
    public IContent getContent() {
        return content;
    }

    public void setContent(RoomAvatarContent content) {
        this.content = content;
    }
}
