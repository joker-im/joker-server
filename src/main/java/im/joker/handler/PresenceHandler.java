package im.joker.handler;

import im.joker.api.vo.PresenceRequest;
import im.joker.device.IDevice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static im.joker.config.filter.AuthFilter.getLoginDevice;
import static im.joker.constants.ImRedisKeys.USER_PRESENCE;

@Component
@Slf4j
public class PresenceHandler {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    public Mono<Void> setPresence(PresenceRequest presenceRequest, IDevice loginDevice) {
        return redisTemplate.opsForValue()
                .set(String.format(USER_PRESENCE, loginDevice.getUsername()),
                        presenceRequest.getPresence(),
                        Duration.ofHours(6)).then();
    }
}
