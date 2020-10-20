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


    public Mono<ServerResponse> setPresence(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(PresenceRequest.class)
                .zipWith(Mono.subscriberContext())
                .flatMap(e -> {
                    IDevice loginDevice = e.getT2().get(getLoginDevice());
                    PresenceRequest p = e.getT1();
                    return redisTemplate.opsForValue()
                            .set(String.format(USER_PRESENCE, loginDevice.getUsername()),
                                    p.getPresence(),
                                    Duration.ofHours(6));
                })
                .flatMap(e -> ServerResponse.ok().build());
    }
}
