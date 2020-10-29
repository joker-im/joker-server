package im.joker.event;

import im.joker.event.content.IContent;
import im.joker.event.content.message.RedactionContent;
import im.joker.event.content.state.*;

public enum EventContentType {


    /**
     * 房间创建
     */
    Creation("m.room.create", CreateContent.class),

    Membership("m.room.member", MembershipContent.class),

    PowerLevel("m.room.power_levels", PowerLevelContent.class),

    Redaction("m.room.redaction", RedactionContent.class),

    RoomName("m.room.name", RoomNameContent.class),

    RoomTopic("m.room.topic", RoomTopicContent.class),

    RoomAvatar("m.room.avatar", RoomAvatarContent.class),

    GuestAccess("m.room.guest_access", GuestAccessContent.class),
    ;

    private final String id;

    private final Class<? extends IContent> eventClass;

    EventContentType(String id, Class<? extends IContent> eventClass) {
        this.id = id;
        this.eventClass = eventClass;
    }

    public String getId() {
        return id;
    }

    public Class<? extends IContent> getEventClass() {
        return eventClass;
    }
}
