package im.joker.helper;

import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.presence.PresenceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static im.joker.constants.ImRedisKeys.ROOM_SUBSCRIBERS_OF_DEVICE;

@Component
@Slf4j
public class RoomSubscribeManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    /**
     * 更新房间与订阅者(设备)的关系, 当一个房间里面被注入了membershipEvent时候, 可以认为是该设备对该房间有订阅的变动
     *
     * @param roomId
     * @param imEvent
     * @return
     */
    public Mono<Void> updateRelation(String roomId, String deviceId, MembershipEvent imEvent) {
        if (imEvent == null) {
            return Mono.empty();
        }
        MembershipContent membershipContent = (MembershipContent) imEvent.getContent();
        return Mono.just(membershipContent)
                .flatMap(e -> {
                    // 当membership为ban或者是leave时候, 取消订阅关系
                    if (MembershipType.Ban.is(e.getMembership()) || MembershipType.Leave.is(e.getMembership())) {
                        return redisTemplate.opsForHash()
                                .remove(ROOM_SUBSCRIBERS_OF_DEVICE, roomId, deviceId).then();
                    }
                    //当房间是加入和被邀请的时候,添加是订阅关系
                    if (MembershipType.Join.is(e.getMembership()) || MembershipType.Invite.is(e.getMembership())) {
                        return redisTemplate.opsForHash()
                                .put(ROOM_SUBSCRIBERS_OF_DEVICE, roomId, deviceId).then();
                    }
                    return Mono.empty();
                });
    }

    /**
     * 更新房间与订阅者的关系
     *
     * @param roomId
     * @param deviceId
     * @param type
     * @return
     */
    public Mono<Void> updateRelation(String roomId, String deviceId, PresenceType type) {
        return Mono.just(type)
                .flatMap(e -> {
                    switch (e) {
                        // 当用户在线的时候, 添加是订阅关系
                        case online -> redisTemplate.opsForHash().put(ROOM_SUBSCRIBERS_OF_DEVICE, roomId, deviceId).then();
                        // 当用户不可用的时候,取消订阅关系
                        case offline, unavailable -> redisTemplate.opsForHash().remove(ROOM_SUBSCRIBERS_OF_DEVICE, roomId, deviceId).then();
                    }
                    return Mono.empty();
                });
    }

    /**
     * 取出设备关心的房间
     *
     * @param deviceId
     * @return
     */
    public Flux<String> retrieveRooms(String deviceId) {

        return Flux.empty();
    }

}
