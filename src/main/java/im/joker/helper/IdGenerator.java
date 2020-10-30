package im.joker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static im.joker.constants.ImRedisKeys.EVENT_STREAM_SEQUENCE;

@Component
@Slf4j
public class IdGenerator {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    @Value("${im.web-domain}")
    private String webDomain;

    /**
     * 获得事件序列id
     *
     * @return
     */
    public Mono<Long> nextEventStreamId() {
        return redisTemplate.opsForValue().increment(EVENT_STREAM_SEQUENCE);
    }


    public String userId(String username) {
        return "@" + username + ":" + webDomain;
    }

    public String newRoomId() {
        return "!" + UUID.randomUUID().toString() + ":" + webDomain;
    }

    public String roomVersion() {
        return "0.0";
    }

}
