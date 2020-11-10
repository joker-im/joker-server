package im.joker.event.room.message;

import im.joker.event.content.IContent;
import im.joker.event.content.message.RedactionContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.message.MessageEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class RedactionEvent extends AbstractRoomEvent {


    private RedactionContent content;


    @Override
    public IContent getContent() {
        return content;
    }

}
