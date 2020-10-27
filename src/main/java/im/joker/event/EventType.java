package im.joker.event;

import im.joker.event.room.RoomCreateEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Stream;

/**
 * 所有事件类型。
 *
 * @author linyurong
 */
public enum EventType {

    /**
     * 房间创建
     */
    Creation("m.room.create", true, RoomCreateEvent.class),

    Unknown(null, false, null);

    public static EventType from(String type) {
        return Stream.of(EventType.values()).filter(t -> StringUtils.equals(t.id, type)).findAny().orElse(Unknown);
    }

    private final String id;
    private final boolean isState;
    private final Class<? extends ImEvent> eventClass;

    EventType(String id, boolean isState, Class<? extends ImEvent> eventClass) {
        this.id = id;
        this.isState = isState;
        this.eventClass = eventClass;
    }


}
