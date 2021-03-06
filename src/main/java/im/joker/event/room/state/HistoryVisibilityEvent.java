package im.joker.event.room.state;

import im.joker.event.content.state.HistoryVisibilityContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class HistoryVisibilityEvent extends AbstractRoomStateEvent {

    private HistoryVisibilityContent content;

    @Override
    public HistoryVisibilityContent getContent() {
        return content;
    }


    public void setContent(HistoryVisibilityContent content) {
        this.content = content;
    }
}
