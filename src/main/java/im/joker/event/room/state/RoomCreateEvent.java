package im.joker.event.room.state;

import im.joker.event.content.state.CreateContent;
import im.joker.event.content.IContent;
import im.joker.event.room.AbstractStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import static im.joker.event.EventType.Creation;


@AllArgsConstructor
@NoArgsConstructor
public class RoomCreateEvent extends AbstractStateEvent {

    private CreateContent createContent;


    @Override
    public IContent getContent() {
        return createContent;
    }

}
