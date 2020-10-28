package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomTopicContent;
import im.joker.event.room.AbstractStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RoomTopicEvent extends AbstractStateEvent {

    private RoomTopicContent content;

    @Override
    public IContent getContent() {
        return content;
    }


}
