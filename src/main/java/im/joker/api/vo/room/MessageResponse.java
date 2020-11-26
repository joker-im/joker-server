package im.joker.api.vo.room;

import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;

import java.util.List;

public class MessageResponse {

    private Long start;

    private Long end;

    private List<AbstractRoomEvent> chunk;

    private List<AbstractRoomStateEvent> state;


    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public List<AbstractRoomEvent> getChunk() {
        return chunk;
    }

    public void setChunk(List<AbstractRoomEvent> chunk) {
        this.chunk = chunk;
    }

    public List<AbstractRoomStateEvent> getState() {
        return state;
    }

    public void setState(List<AbstractRoomStateEvent> state) {
        this.state = state;
    }
}
