package im.joker.handler;

import im.joker.api.vo.presence.PresenceRequest;
import im.joker.device.IDevice;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.presence.PresenceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static im.joker.constants.ImRedisKeys.USER_PRESENCE;

@Component
@Slf4j
public class PresenceHandler {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    public Mono<Void> setPresence(PresenceRequest presenceRequest, IDevice loginDevice) {
        Optional<PresenceType> exists = Arrays.stream(PresenceType.values())
                .filter(presenceType -> presenceType.name().equals(presenceRequest.getPresence()))
                .findAny();
        if (exists.isEmpty()) {
            return Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST));
        }
        return redisTemplate.opsForValue()
                .set(String.format(USER_PRESENCE, loginDevice.getUsername()),
                        presenceRequest.getPresence(),
                        Duration.ofDays(1L)).then();
    }
}
