package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.PowerLevelContent;
import im.joker.event.room.AbstractStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class PowerLevelEvent extends AbstractStateEvent {

    private PowerLevelContent content;

    @Override
    public IContent getContent() {
        return content;
    }

}
