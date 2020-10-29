package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.CreateContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateEvent extends AbstractRoomStateEvent {

    private CreateContent createContent;


    @Override
    public IContent getContent() {
        return createContent;
    }

}
