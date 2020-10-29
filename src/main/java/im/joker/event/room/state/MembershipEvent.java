package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MembershipEvent extends AbstractRoomStateEvent {

    private MembershipContent content;


    @Override
    public IContent getContent() {
        return content;
    }


}
