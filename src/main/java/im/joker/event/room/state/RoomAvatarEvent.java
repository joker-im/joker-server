package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.RoomAvatarContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.IRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class RoomAvatarEvent extends AbstractRoomStateEvent {


    private RoomAvatarContent content;


    @Override
    public IContent getContent() {
        return content;
    }

}
