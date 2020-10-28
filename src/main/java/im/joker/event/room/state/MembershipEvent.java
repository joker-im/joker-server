package im.joker.event.room.state;

import im.joker.event.content.IContent;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractStateEvent;
import im.joker.event.room.UnsignedData;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class MembershipEvent extends AbstractStateEvent {

    private MembershipContent content;

    private UnsignedData unsigned;


    @Override
    public IContent getContent() {
        return content;
    }

}
