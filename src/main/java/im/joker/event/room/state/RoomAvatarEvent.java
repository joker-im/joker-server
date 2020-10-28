package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomAvatarContent;
import im.joker.event.room.AbstractStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RoomAvatarEvent extends AbstractStateEvent {

    private RoomAvatarContent content;

    @Override
    public IContent getContent() {
        return content;
    }
}
