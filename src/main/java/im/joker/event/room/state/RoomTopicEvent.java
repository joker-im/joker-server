package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomTopicContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.IRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RoomTopicEvent extends AbstractRoomStateEvent {

    private RoomTopicContent content;


    @Override
    public IContent getContent() {
        return content;
    }


}
