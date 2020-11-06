package im.joker.event;

import im.joker.event.content.state.*;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.state.*;
import im.joker.helper.IdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class ImEventBuilder {

    @Autowired
    private IdGenerator idGenerator;


    public void setCommonEventFiled(AbstractRoomEvent roomEvent, String roomId, String sender, LocalDateTime time) {
        roomEvent.setEventId(UUID.randomUUID().toString());
        roomEvent.setRoomId(roomId);
        roomEvent.setTransactionId(UUID.randomUUID().toString());
        roomEvent.setSender(sender);
        roomEvent.setOriginServerTs(time);
    }


    public RoomCreateEvent roomCreateEvent(String creator, String roomId, String sender, LocalDateTime time) {
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
        setCommonEventFiled(roomCreateEvent, roomId, sender, time);
        return roomCreateEvent;
    }

    public MembershipEvent membershipEvent(String roomId, LocalDateTime time, String sender,
                                           String stateKey, String displayName, String avatarUrl, MembershipType membership) {
        MembershipContent mContent = MembershipContent
                .builder()
                .avatarUrl(avatarUrl)
                .membership(membership.name().toLowerCase())
                .displayName(displayName)
                .build();

        MembershipEvent membershipEvent = MembershipEvent.builder()
                .type(EventType.Membership.getId())
                .stateKey(stateKey)
                .content(mContent).build();
        setCommonEventFiled(membershipEvent, roomId, sender, time);
        return membershipEvent;
    }

    public RoomNameEvent roomNameEvent(String roomName, String roomId, String sender, LocalDateTime time) {
        RoomNameContent roomNameContent = RoomNameContent.builder()
                .name(roomName).build();
        RoomNameEvent roomNameEvent = RoomNameEvent.builder()
                .content(roomNameContent)
                .type(EventType.RoomName.getId())
                .stateKey("")
                .build();
        setCommonEventFiled(roomNameEvent, roomId, sender, time);
        return roomNameEvent;
    }

    public RoomTopicEvent roomTopicEvent(String topic, String roomId, String sender, LocalDateTime time) {
        RoomTopicContent topicContent = RoomTopicContent.builder().topic(topic).build();
        RoomTopicEvent topicEvent = RoomTopicEvent.builder().content(topicContent)
                .type(EventType.RoomTopic.getId())
                .stateKey("").build();
        setCommonEventFiled(topicEvent, roomId, sender, time);
        return topicEvent;
    }

    public PowerLevelEvent powerLevelEvent(int ban, int invite, int kick, int redact, int state, Map<String, Integer> events,
                                           int message, int room, Map<String, Integer> users, String roomId, String sender,
                                           LocalDateTime time) {
        PowerLevelContent pc = PowerLevelContent.builder()
                .ban(ban)
                .invite(invite)
                .kick(kick)
                .events(events)
                .redact(redact)
                .stateDefault(state)
                .eventDefault(message)
                .users(users)
                .notifications(PowerLevelContent.RoomNotificationPower.builder().room(room).build())
                .build();
        PowerLevelEvent pe = PowerLevelEvent.builder()
                .type(EventType.PowerLevel.getId())
                .content(pc).build();
        setCommonEventFiled(pe, roomId, sender, time);
        return pe;
    }

    /**
     * 创房时默认发的powerLevelEvent
     *
     * @param roomId
     * @param creator
     * @param time
     * @return
     */
    public PowerLevelEvent defaultPowerLevelEvent(String roomId, String creator, LocalDateTime time) {
        Map<String, Integer> users = Map.of(creator, 100);
        return powerLevelEvent(60, 50, 60, 50, 50, null, 0, 50, users, roomId, creator, time);
    }

    public RoomJoinRuleEvent roomJoinRuleEvent(RoomJoinRuleType roomJoinRuleType, String roomId, String sender, LocalDateTime time) {
        RoomJoinRuleContent content = RoomJoinRuleContent
                .builder()
                .joinRule(roomJoinRuleType.name().toLowerCase())
                .build();

        RoomJoinRuleEvent event = RoomJoinRuleEvent.builder()
                .type(EventType.RoomJoinRule.getId())
                .content(content)
                .build();
        setCommonEventFiled(event, roomId, sender, time);
        return event;
    }

    /**
     * 创房时默认的房间历史是否可以查看事件
     *
     * @param roomId
     * @param sender
     * @return
     */
    public HistoryVisibilityEvent defaultHistoryVisibilityEvent(String roomId, String sender, LocalDateTime time) {
        HistoryVisibilityContent hvc = HistoryVisibilityContent.builder().historyVisibility("joined").build();
        HistoryVisibilityEvent hvEvent = HistoryVisibilityEvent.builder().content(hvc).build();
        setCommonEventFiled(hvEvent, roomId, sender, time);
        return hvEvent;
    }
}
