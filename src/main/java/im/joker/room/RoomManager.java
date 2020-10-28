package im.joker.room;

import im.joker.api.vo.room.CreateRoomRequest;
import im.joker.api.vo.room.CreateRoomResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
@Slf4j
public class RoomManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    public Mono<CreateRoomResponse> createRoom(CreateRoomRequest createRoomRequest) {
        return null;
    }

}
