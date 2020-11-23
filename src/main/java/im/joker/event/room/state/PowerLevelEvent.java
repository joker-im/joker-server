package im.joker.event.room.state;

import im.joker.event.content.state.PowerLevelContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class PowerLevelEvent extends AbstractRoomStateEvent {


    private PowerLevelContent content;


    @Override
    public PowerLevelContent getContent() {
        return content;
    }

    public void setContent(PowerLevelContent content) {
        this.content = content;
    }
}
