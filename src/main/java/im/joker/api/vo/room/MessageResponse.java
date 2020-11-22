package im.joker.api.vo.room;

import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;

import java.util.List;

public class MessageResponse {

    private Integer start;

    private Integer end;

    private List<AbstractRoomEvent> chunk;

    private List<AbstractRoomStateEvent> state;


    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Integer getEnd() {
        return end;
    }

    public void setEnd(Integer end) {
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
