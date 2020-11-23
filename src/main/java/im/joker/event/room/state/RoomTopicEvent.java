package im.joker.event.room.state;

import im.joker.event.content.state.RoomTopicContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class RoomTopicEvent extends AbstractRoomStateEvent {


    private RoomTopicContent content;


    @Override
    public RoomTopicContent getContent() {
        return content;
    }


    public void setContent(RoomTopicContent content) {
        this.content = content;
    }
}
