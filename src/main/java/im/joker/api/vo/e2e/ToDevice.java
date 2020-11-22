package im.joker.api.vo.e2e;

import im.joker.event.room.AbstractRoomEvent;

import java.util.List;

public class ToDevice {
    private List<AbstractRoomEvent> events;

    public List<AbstractRoomEvent> getEvents() {
        return events;
    }

    public void setEvents(List<AbstractRoomEvent> events) {
        this.events = events;
    }
}
