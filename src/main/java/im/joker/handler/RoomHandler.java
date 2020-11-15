package im.joker.handler;

import im.joker.api.vo.room.*;
import im.joker.device.IDevice;
import im.joker.event.ImEvent;
import im.joker.event.MembershipType;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.helper.IdGenerator;
import im.joker.room.RoomManager;
import im.joker.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static im.joker.constants.ImRedisKeys.MESSAGE_LOCK;


@Service
@Slf4j
public class RoomHandler {


    @Autowired
    private RoomManager roomManager;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private RedissonReactiveClient redissonClient;


    public Mono<CreateRoomResponse> createRoom(IDevice loginDevice, CreateRoomRequest createRoomRequest) {

        return roomManager.createRoom(loginDevice, createRoomRequest)
                .map(e -> CreateRoomResponse.builder().roomId(e.getRoomId()).build());
    }

    public Mono<JoinedRoomResponse> searchJoinedRooms(IDevice loginDevice) {
        return roomManager.membershipAboutRooms(loginDevice.getUserId(), content -> MembershipType.Join.is(content.getMembership()))
                .map(roomIds -> JoinedRoomResponse.builder().joinedRooms(roomIds).build());
    }


    public Mono<Void> inviteToRoom(String targetRoomId, InviteRequest inviteRequest, IDevice loginDevice) {
        return roomManager.inviteToRoom(targetRoomId, inviteRequest.getUserId(), loginDevice).then();
    }

    public Mono<JoinRoomResponse> joinRoom(IDevice loginDevice, String targetRoomId) {
        Mono<ImEvent> join = roomManager.joinRoom(loginDevice, targetRoomId);
        return join.map(e -> JoinRoomResponse.builder().roomId(targetRoomId).build());
    }


    public Mono<Void> levelRoom(IDevice loginDevice, String targetRoomId) {
        return roomManager.levelRoom(loginDevice, targetRoomId).then();
    }

    public Mono<Void> kick(IDevice loginDevice, KickRequest kickRequest, String roomId) {
        String sender = loginDevice.getUserId();
        String targetUserId = kickRequest.getUserId();
        return roomManager.kickMember(loginDevice, targetUserId, kickRequest.getReason(), roomId);
    }

    public Mono<String> sendMessageEvent(IDevice loginDevice, AbstractRoomEvent messageEvent) {
        return redissonClient.getLock(String.format(MESSAGE_LOCK, messageEvent.getRoomId())).lock(2, TimeUnit.SECONDS)
                .flatMap(e -> idGenerator.nextEventStreamId()
                        .flatMap(id -> {
                            messageEvent.setSender(loginDevice.getUserId());
                            messageEvent.setStreamId(id);
                            messageEvent.setEventId(UUID.randomUUID().toString());
                            messageEvent.setOriginServerTs(LocalDateTime.now());
                            log.debug("收到聊天消息{}", GsonUtils.get().toJson(messageEvent));
                            return roomManager.sendMessageEvent(loginDevice, messageEvent);
                        }))
                .doFinally((s) -> redissonClient.getLock(String.format(MESSAGE_LOCK, messageEvent.getRoomId())).unlock().subscribe());
    }
}

