package im.joker.event.room.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.IContent;
import im.joker.event.room.AbstractMessageEvent;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MessageEvent extends AbstractMessageEvent {

    private AbstractMessageContent content;

    @Override
    public IContent getContent() {
        return content;
    }

}
