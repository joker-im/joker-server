package im.joker.event.room;

import im.joker.event.ContentEvent;

/**
 * @author linyurong
 **/
public abstract class AbstractRoomEvent<T> extends ContentEvent<T> implements IRoomEvent<T> {
    protected String roomId;
    protected String stateKey;

    public AbstractRoomEvent() {}

    public AbstractRoomEvent(String type, String sender, Long originServerTs) {
        super(type, sender, originServerTs, null);
    }

    public AbstractRoomEvent(String type, String sender, Long originServerTs, T content) {
        super(type, sender, originServerTs, content);
    }

    @Override
    public String getStateKey() {
        return stateKey;
    }

    public void setStateKey() {
        this.stateKey = stateKey;
    }

    @Override
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
