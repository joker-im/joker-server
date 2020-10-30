package im.joker.handler;

import im.joker.api.vo.room.*;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.room.IRoom;
import im.joker.room.RoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;


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
        return null;
    }


    public Mono<ServerResponse> createRoom(ServerRequest serverRequest) {
        return null;
    }


    public Mono<Void> inviteToRoom(String targetRoomId, InviteRequest inviteRequest, IDevice loginDevice) {
        return null;
    }

    public Mono<JoinRoomResponse> joinRoom(IDevice loginDevice, String targetRoomId) {
        return null;
    }

    public Mono<Void> levelRoom(IDevice loginDevice, String targetRoomId) {
        return null;
    }

    public Mono<ServerResponse> kickRoom(ServerRequest serverRequest) {
        return null;
    }
}

