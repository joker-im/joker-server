package im.joker.event;

import im.joker.event.room.message.MessageEvent;
import im.joker.event.room.state.*;
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

    Membership("m.room.member", true, MembershipEvent.class),

    PowerLevel("m.room.power_levels", true, PowerLevelEvent.class),

    Redaction("m.room.redaction", false, RedactionEvent.class),

    RoomMessage("m.room.message", false, MessageEvent.class),

    RoomName("m.room.name", true, RoomNameEvent.class),

    RoomTopic("m.room.topic", true, RoomTopicEvent.class),

    RoomAvatar("m.room.avatar", true, RoomAvatarEvent.class),

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

    public String getId() {
        return id;
    }

    public boolean isState() {
        return isState;
    }

    public Class<? extends ImEvent> getEventClass() {
        return eventClass;
    }

}
