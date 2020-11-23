package im.joker.event.room.state;

import im.joker.event.content.state.EncryptionContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class EncryptionEvent extends AbstractRoomStateEvent {


    private EncryptionContent content;

    @Override
    public EncryptionContent getContent() {
        return content;
    }

    public void setContent(EncryptionContent content) {
        this.content = content;
    }
}
