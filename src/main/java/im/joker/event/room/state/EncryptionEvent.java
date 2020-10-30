package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.EncryptionContent;
import im.joker.event.room.AbstractRoomStateEvent;
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
public class EncryptionEvent extends AbstractRoomStateEvent {



    private EncryptionContent content;

    @Override
    public IContent getContent() {
        return content;
    }
}
