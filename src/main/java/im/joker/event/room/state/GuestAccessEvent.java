package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.GuestAccessContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public class GuestAccessEvent extends AbstractRoomStateEvent  {




    private GuestAccessContent content;


    @Override
    public IContent getContent() {
        return content;
    }
}
