package im.joker.handler;

import im.joker.api.vo.room.*;
import im.joker.device.IDevice;
import im.joker.event.EventType;
import im.joker.event.ImEvent;
import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.room.IRoom;
import im.joker.room.RoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
public class RoomHandler {


    @Autowired
    private RoomManager roomManager;


    public Mono<CreateRoomResponse> createRoom(IDevice loginDevice, CreateRoomRequest createRoomRequest) {

        return roomManager.createRoom(loginDevice, createRoomRequest)
                .map(e -> CreateRoomResponse.builder().roomId(e.getRoomId()).build());
    }

    public Mono<JoinedRoomResponse> searchJoinedRooms(IDevice loginDevice) {
        Flux<ImEvent> eventFlux = roomManager.findEvents(EventType.Membership, loginDevice.getUserId());
        return eventFlux
                .filter(e -> {
                    MembershipContent content = (MembershipContent) e.getContent();
                    return MembershipType.Join.is(content.getMembership());
                })
                .collectList()
                .map(events -> {
                    List<String> destRoomIds = events
                            .stream()
                            .map(e -> ((MembershipEvent) e).getRoomId())
                            .distinct()
                            .collect(Collectors.toList());
                    return JoinedRoomResponse.builder().joinedRooms(destRoomIds).build();
                });
    }


    public Mono<Void> inviteToRoom(String targetRoomId, InviteRequest inviteRequest, IDevice loginDevice) {
        return roomManager.inviteToRoom(targetRoomId, inviteRequest.getUserId(), loginDevice.getUserId()).then();
    }

    public Mono<JoinRoomResponse> joinRoom(IDevice loginDevice, String targetRoomId) {
        Mono<IRoom> roomMono = roomManager.findRoom(targetRoomId);

        return null;
    }

    public Mono<Void> levelRoom(IDevice loginDevice, String targetRoomId) {
        return null;
    }

    public Mono<ServerResponse> kickRoom(ServerRequest serverRequest) {
        return null;
    }
}

