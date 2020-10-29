package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.message.RedactionContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RedactionEvent extends AbstractRoomStateEvent {

    private RedactionContent content;


    @Override
    public IContent getContent() {
        return content;
    }

}
