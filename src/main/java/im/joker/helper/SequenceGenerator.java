package im.joker.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static im.joker.constants.ImRedisKeys.EVENT_STREAM_SEQUENCE;

@Component
@Slf4j
public class SequenceGenerator {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    /**
     * 获得事件序列id
     *
     * @return
     */
    public Mono<Long> nextEventStreamId() {
        return redisTemplate.opsForValue().increment(EVENT_STREAM_SEQUENCE);
    }

}
