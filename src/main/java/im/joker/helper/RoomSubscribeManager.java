package im.joker.helper;

import im.joker.device.IDevice;
import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.presence.PresenceType;
import im.joker.room.RoomManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static im.joker.constants.ImRedisKeys.ROOM_SUBSCRIBERS_OF_DEVICE;

@Component
@Slf4j
public class RoomSubscribeManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Autowired
    private RoomManager roomManager;


    /**
     * 更新房间与订阅者(设备)的关系, 当一个房间里面被注入了membershipEvent时候, 可以认为是该设备对该房间有订阅的变动
     *
     * @param roomEvent
     * @return
     */
    public Mono<Void> updateRelation(String deviceId, AbstractRoomEvent roomEvent) {
        if (!(roomEvent instanceof MembershipEvent)) {
            return Mono.empty();
        }
        MembershipContent membershipContent = (MembershipContent) roomEvent.getContent();
        return Mono.just(membershipContent)
                .flatMap(e -> {
                    // 当membership为ban或者是leave时候, 取消订阅关系
                    if (MembershipType.Ban.is(e.getMembership()) || MembershipType.Leave.is(e.getMembership())) {
                        return redisTemplate.opsForSet()
                                .remove(String.format(ROOM_SUBSCRIBERS_OF_DEVICE, roomEvent.getRoomId()), deviceId).then();
                    }
                    //当房间是加入和被邀请的时候,添加是订阅关系
                    if (MembershipType.Join.is(e.getMembership()) || MembershipType.Invite.is(e.getMembership())) {
                        return redisTemplate.opsForSet()
                                .add(String.format(ROOM_SUBSCRIBERS_OF_DEVICE, roomEvent.getRoomId()), deviceId).then();
                    }
                    return Mono.
                            empty();
                });
    }

    /**
     * 用户状态更新房间与订阅者的关系
     *
     * @param loginDevice
     * @param type
     * @return
     */
    public Mono<Void> updateRelation(IDevice loginDevice, PresenceType type) {
        return Mono.just(type)
                .flatMap(e -> {
                    switch (e) {
                        // 当用户在线的时候, 添加是订阅关系
                        case online ->
                                // 找出该用户所有关心的房间,并添加订阅
                                roomManager.membershipAboutRooms(loginDevice.getUserId(),
                                        content -> MembershipType.Invite.is(content.getMembership()) || MembershipType.Join.is(content.getMembership()))
                                        .flatMapMany(Flux::fromIterable)
                                        .flatMap(roomId -> redisTemplate.opsForSet().add(String.format(ROOM_SUBSCRIBERS_OF_DEVICE, roomId), loginDevice.getDeviceId()).then());

                        // 当用户不可用的时候,取消订阅关系
                        case offline, unavailable -> roomManager.membershipAboutRooms(loginDevice.getUserId(),
                                content -> MembershipType.Invite.is(content.getMembership()) || MembershipType.Join.is(content.getMembership()))
                                .flatMapMany(Flux::fromIterable)
                                .flatMap(roomId -> redisTemplate.opsForSet().remove(String.format(ROOM_SUBSCRIBERS_OF_DEVICE, roomId), loginDevice.getDeviceId()).then());
                    }
                    return Mono.empty();
                });
    }

    /**
     * 取出设备关心的房间ids
     *
     * @param deviceId
     * @return
     */
    public Flux<String> retrieveRooms(String deviceId) {
        ScanOptions scanOptions = ScanOptions.scanOptions().match(ROOM_SUBSCRIBERS_OF_DEVICE.replace("%s", "*")).build();
        // 扫描所有房间的key
        return redisTemplate.scan(scanOptions)
                // 判断该deviceId是当前房间的成员否,如果是,那么放到一个地方收集
                .flatMap(key -> redisTemplate.opsForSet().isMember(key, deviceId).flatMapMany(is -> {
                    if (is) {
                        // 取出对应key
                        return Flux.just(key.replace(ROOM_SUBSCRIBERS_OF_DEVICE.replace("%s", ""), ""));
                    } else {
                        return Flux.empty();
                    }
                }));

    }

}
