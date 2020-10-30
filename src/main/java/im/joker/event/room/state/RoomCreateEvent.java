package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.CreateContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class RoomCreateEvent extends AbstractRoomStateEvent {


    private CreateContent content;


    @Override
    public IContent getContent() {
        return content;
    }



}
