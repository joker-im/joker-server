package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomNameContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.IRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RoomNameEvent extends AbstractRoomStateEvent {

    private RoomNameContent content;


    @Override
    public IContent getContent() {
        return content;
    }

}
