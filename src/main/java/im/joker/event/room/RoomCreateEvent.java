package im.joker.event.room;

import im.joker.event.content.CreateContent;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class RoomCreateEvent extends AbsStateEvent {

    private CreateContent createContent;

    @Override
    public IContent getContent() {
        return createContent;
    }
}
