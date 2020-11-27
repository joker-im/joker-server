package im.joker.event;

import im.joker.event.room.message.MessageEvent;
import im.joker.event.room.message.RedactionEvent;
import im.joker.event.room.other.FullReadMarkerEvent;
import im.joker.event.room.other.ReceiptEvent;
import im.joker.event.room.other.TypingEvent;
import im.joker.event.room.state.*;

/**
 * 所有事件类型。
 *
 * @author linyurong
 */
public enum EventType {

    /**
     * 房间主要事件
     */
    // 创房
    Creation("m.room.create", true, RoomCreateEvent.class),
    // 成员变动
    Membership("m.room.member", true, MembershipEvent.class),
    // 房间权限
    PowerLevel("m.room.power_levels", true, PowerLevelEvent.class),
    // 编辑
    Redaction("m.room.redaction", false, RedactionEvent.class),
    // 消息
    RoomMessage("m.room.message", false, MessageEvent.class),
    // 改房间名
    RoomName("m.room.name", true, RoomNameEvent.class),
    // 房间话题
    RoomTopic("m.room.topic", true, RoomTopicEvent.class),
    // 房间头像
    RoomAvatar("m.room.avatar", true, RoomAvatarEvent.class),
    // 房间来宾客户是否可以访问
    GuestAccess("m.room.guest_access", true, GuestAccessEvent.class),
    // 房间加密事件
    Encryption("m.room.encryption", true, EncryptionEvent.class),
    // 房间加入规则事件
    RoomJoinRule("m.room.join_rules", true, RoomJoinRuleEvent.class),
    // 房间历史消息
    HistoryVisibility("m.room.history_visibility", true, HistoryVisibilityEvent.class),


    /**
     * 房间其他事件
     */
    // 打字
    Typing("m.typing", false, TypingEvent.class),

    // 已读某条事件
    Receipt(" m.receipt", false, ReceiptEvent.class),

    // 已读某个房间标志事件
    MFullRead("m.fully_read", false, FullReadMarkerEvent.class),


    ;


    public boolean is(String type) {
        return this.getId().equals(type);
    }

    public static EventType findByType(String type) {
        for (EventType value : EventType.values()) {
            if (value.getId().equals(type)) {
                return value;
            }
        }
        return null;
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
