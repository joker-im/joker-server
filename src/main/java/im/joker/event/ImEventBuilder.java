package im.joker.event;

import im.joker.event.content.state.CreateContent;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.content.state.RoomNameContent;
import im.joker.event.content.state.RoomTopicContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.event.room.state.RoomCreateEvent;
import im.joker.event.room.state.RoomNameEvent;
import im.joker.event.room.state.RoomTopicEvent;
import im.joker.helper.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImEventBuilder {

    @Autowired
    private IdGenerator idGenerator;


    public void setCommonEventFiled(AbstractRoomEvent roomEvent, String roomId, String sender, Long originTs) {
        roomEvent.setEventId(UUID.randomUUID().toString());
        roomEvent.setRoomId(roomId);
        roomEvent.setTransactionId(UUID.randomUUID().toString());
        roomEvent.setSender(sender);
        roomEvent.setOriginServerTs(originTs);
    }


    public RoomCreateEvent roomCreateEvent(String creator, String roomId, String sender, Long originTs) {
        CreateContent createContent = CreateContent.builder()
                .creator(creator)
                .mFederate(false)
                .roomVersion(idGenerator.roomVersion())
                .build();
        RoomCreateEvent roomCreateEvent = RoomCreateEvent.builder()
                .content(createContent)
                .type(EventType.Creation.getId())
                .stateKey("")
                .build();
        setCommonEventFiled(roomCreateEvent, roomId, sender, originTs);
        return roomCreateEvent;
    }

    public MembershipEvent membershipEvent(String roomId, Long originTs, String sender,
                                           String stateKey, String displayName, String avatarUrl, MembershipType membership) {
        MembershipContent mContent = MembershipContent
                .builder()
                .avatarUrl(avatarUrl)
                .membership(membership.name())
                .displayName(displayName)
                .build();

        MembershipEvent membershipEvent = MembershipEvent.builder()
                .type(EventType.Membership.getId())
                .stateKey(stateKey)
                .content(mContent).build();
        setCommonEventFiled(membershipEvent, roomId, sender, originTs);
        return membershipEvent;
    }

    public RoomNameEvent roomNameEvent(String roomName, String roomId, String sender, Long originTs) {
        RoomNameContent roomNameContent = RoomNameContent.builder()
                .name(roomName).build();
        RoomNameEvent roomNameEvent = RoomNameEvent.builder()
                .content(roomNameContent)
                .type(EventType.RoomName.getId())
                .stateKey("")
                .build();
        setCommonEventFiled(roomNameEvent, roomId, sender, originTs);
        return roomNameEvent;
    }

    public RoomTopicEvent roomTopicEvent(String topic, String roomId, String sender, Long nowTimestamp) {
        RoomTopicContent topicContent = RoomTopicContent.builder().topic(topic).build();
        RoomTopicEvent topicEvent = RoomTopicEvent.builder().content(topicContent)
                .type(EventType.RoomTopic.getId())
                .stateKey("").build();
        setCommonEventFiled(topicEvent, roomId, sender, nowTimestamp);
        return topicEvent;
    }
}
