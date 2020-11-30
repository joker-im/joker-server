package im.joker.api.vo.room;

import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;

import java.util.List;

public class FindMembersResponse {

    private List<MembershipEvent> chunk;

    public List<MembershipEvent> getChunk() {
        return chunk;
    }

    public void setChunk(List<MembershipEvent> chunk) {
        this.chunk = chunk;
    }
}
