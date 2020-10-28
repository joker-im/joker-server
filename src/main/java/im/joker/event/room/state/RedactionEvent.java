package im.joker.event.room.state;

import im.joker.event.EventType;
import im.joker.event.content.IContent;
import im.joker.event.content.state.RedactionContent;
import im.joker.event.room.AbstractMessageEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class RedactionEvent extends AbstractMessageEvent {

    private RedactionContent content;

    @Override
    public IContent getContent() {
        return content;
    }

    @Override
    public String getType() {
        return EventType.Redaction.getId();
    }
}
