package im.joker.api.vo;

import im.joker.event.room.AbstractRoomEvent;

import java.util.List;


public class RoomEvents {

    private String roomId;

    private List<AbstractRoomEvent> sliceLastEvents;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<AbstractRoomEvent> getSliceLastEvents() {
        return sliceLastEvents;
    }

    public void setSliceLastEvents(List<AbstractRoomEvent> sliceLastEvents) {
        this.sliceLastEvents = sliceLastEvents;
    }
}
