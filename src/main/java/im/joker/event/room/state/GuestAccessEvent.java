package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.GuestAccessContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class GuestAccessEvent extends AbstractRoomStateEvent  {


    private GuestAccessContent content;


    @Override
    public IContent getContent() {
        return content;
    }
}
