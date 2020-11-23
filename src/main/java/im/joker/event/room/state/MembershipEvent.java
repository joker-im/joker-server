package im.joker.event.room.state;

import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomStateEvent;

public class MembershipEvent extends AbstractRoomStateEvent {



    private MembershipContent content;


    @Override
    public MembershipContent getContent() {
        return content;
    }

    public void setContent(MembershipContent content) {
        this.content = content;
    }
}
