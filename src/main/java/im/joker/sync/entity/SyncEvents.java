package im.joker.sync.entity;

import im.joker.event.room.IRoomEvent;

import java.util.List;

/**
 * @Author LinYuRong
 * @Date 2020/11/3 12:50
 * @Version 1.0
 */
public class SyncEvents {
    private String roomId;
    private List<IRoomEvent> events;

    public SyncEvents(String roomId, List<IRoomEvent> events) {
        this.roomId = roomId;
        this.events = events;
    }

    public String getRoomId() {
        return roomId;
    }

    public List<IRoomEvent> getEvents() {
        return events;
    }
}
