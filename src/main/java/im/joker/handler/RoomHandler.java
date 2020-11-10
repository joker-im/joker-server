package im.joker.handler;

import com.google.common.collect.Lists;
import im.joker.api.vo.room.*;
import im.joker.device.IDevice;
import im.joker.event.EventType;
import im.joker.event.ImEvent;
import im.joker.event.ImEventBuilder;
import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.message.MessageEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.helper.IdGenerator;
import im.joker.room.RoomManager;
import im.joker.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RoomHandler {


    @Autowired
    private RoomManager roomManager;
    @Autowired
    private IdGenerator idGenerator;


    public Mono<CreateRoomResponse> createRoom(IDevice loginDevice, CreateRoomRequest createRoomRequest) {

        return roomManager.createRoom(loginDevice, createRoomRequest)
                .map(e -> CreateRoomResponse.builder().roomId(e.getRoomId()).build());
    }

    public Mono<JoinedRoomResponse> searchJoinedRooms(IDevice loginDevice) {
        Flux<ImEvent> eventFlux = roomManager.findEvents(EventType.Membership, loginDevice.getUserId());
        return eventFlux
                .collectList()
                .map(events -> {
                    Map<String, List<MembershipEvent>> roomMemberEventMap = events
                            .stream()
                            .filter(e -> e instanceof MembershipEvent && StringUtils.equals(((MembershipEvent) e).getStateKey(), loginDevice.getUserId()))
                            .map(e -> (MembershipEvent) e)
                            .sorted((o1, o2) -> o2.getStreamId().compareTo(o1.getStreamId()))
                            .collect(Collectors.groupingBy(MembershipEvent::getRoomId, Collectors.toList()));

                    List<String> destRoomIds = Lists.newArrayList();
                    roomMemberEventMap.forEach((roomId, roomMemberEvents) -> {
                        if (CollectionUtils.isEmpty(roomMemberEvents)) {
                            return;
                        }
                        Optional<MembershipEvent> latestJoinEvent = roomMemberEvents
                                .stream()
                                .filter(e -> MembershipType.Join.is(((MembershipContent) e.getContent()).getMembership()))
                                .findFirst();
                        Optional<MembershipEvent> latestLeaveEvent = roomMemberEvents
                                .stream()
                                .filter(e -> MembershipType.Leave.is(((MembershipContent) e.getContent()).getMembership()))
                                .findFirst();
                        // join为空的不要
                        if (latestJoinEvent.isEmpty()) {
                            return;
                        }
                        // join在leave之前就不要
                        if (latestLeaveEvent.isPresent() && latestJoinEvent.get().getOriginServerTs().isBefore(latestLeaveEvent.get().getOriginServerTs())) {
                            return;
                        }
                        destRoomIds.add(roomId);

                    });

                    return JoinedRoomResponse.builder().joinedRooms(destRoomIds).build();
                });
    }


    public Mono<Void> inviteToRoom(String targetRoomId, InviteRequest inviteRequest, IDevice loginDevice) {
        return roomManager.inviteToRoom(targetRoomId, inviteRequest.getUserId(), loginDevice.getUserId()).then();
    }

    public Mono<JoinRoomResponse> joinRoom(IDevice loginDevice, String targetRoomId) {
        Mono<ImEvent> join = roomManager.joinRoom(loginDevice.getUserId(), targetRoomId);
        return join.map(e -> JoinRoomResponse.builder().roomId(targetRoomId).build());
    }


    public Mono<Void> levelRoom(IDevice loginDevice, String targetRoomId) {
        return roomManager.levelRoom(loginDevice.getUserId(), targetRoomId).then();
    }

    public Mono<Void> kick(IDevice loginDevice, KickRequest kickRequest, String roomId) {
        String sender = loginDevice.getUserId();
        String targetUserId = kickRequest.getUserId();
        return roomManager.kickMember(sender, targetUserId, kickRequest.getReason(), roomId);
    }

    public Mono<String> sendMessageEvent(IDevice loginDevice, AbstractRoomEvent messageEvent) {
        return idGenerator.nextEventStreamId()
                .flatMap(id -> {
                    messageEvent.setSender(loginDevice.getUserId());
                    messageEvent.setStreamId(id);
                    messageEvent.setEventId(UUID.randomUUID().toString());
                    messageEvent.setOriginServerTs(LocalDateTime.now());
                    log.debug("收到聊天消息{}", GsonUtils.get().toJson(messageEvent));
                    return roomManager.sendMessageEvent(messageEvent);
                });
    }
}

