package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.HistoryVisibilityContent;
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
public class HistoryVisibilityEvent extends AbstractRoomStateEvent {

    private HistoryVisibilityContent content;

    @Override
    public IContent getContent() {
        return content;
    }
}
