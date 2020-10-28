package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomNameContent;
import im.joker.event.room.AbstractStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RoomNameEvent extends AbstractStateEvent {

    private RoomNameContent content;

    @Override
    public IContent getContent() {
        return content;
    }

}
