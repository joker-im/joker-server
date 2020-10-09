package im.joker.event;

import im.joker.event.room.RoomCreateEvent;
import im.joker.event.room.RoomMembershipEvent;
import im.joker.event.room.RoomMessageEvent;
import im.joker.util.GsonUtils;
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
    /**
     * 成员变化
     */
    Membership("m.room.member", true, RoomMembershipEvent.class),
    /**
     * 房间的对话消息
     */
    Message("m.room.message", false, RoomMessageEvent.class),

    Unknown(null, false, null);

    public static EventType from(String type) {
        return Stream.of(EventType.values()).filter(t -> StringUtils.equals(t.id, type)).findAny().orElse(Unknown);
    }

    private final String id;
    private final boolean isState;
    private final Class<? extends IContentEvent> eventClass;

    EventType(String id, boolean isState, Class<? extends IContentEvent> eventClass) {
        this.id = id;
        this.isState = isState;
        this.eventClass = eventClass;
    }

    public String get() {
        return id;
    }

    public boolean is(String type) {
        return StringUtils.equals(this.id, type);
    }

    public boolean isState() {
        return isState;
    }

    public Class<? extends IContentEvent> getEventClass() {
        return eventClass;
    }

    public static IContentEvent convertToSpecificEvent(String eventType, String rawJson) {
        EventType type = EventType.from(eventType);
        if (type != Unknown) {
            if (type.getEventClass() != null) {
                return GsonUtils.get().fromJson(rawJson, type.getEventClass());
            } else {
                return null;
            }
        }
        return null;
    }
}
