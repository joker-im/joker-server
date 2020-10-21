package im.joker.device;

import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static im.joker.constants.ImRedisKeys.DEVICE_TOKEN;
import static im.joker.constants.ImRedisKeys.TOKEN_USER;

@Component
public class DeviceManager {


    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    /**
     * 在redis中查询是否存在accessToken,存在则返回
     *
     * @param deviceId
     * @param username
     * @param name
     * @return
     */
    public Mono<IDevice> findOrCreateDevice(@Nullable String deviceId, String username, @Nullable String name) {
        deviceId = StringUtils.defaultIfBlank(deviceId, UUID.randomUUID().toString());
        String finalDeviceId = deviceId;
        return redisTemplate.opsForValue()
                // redis中找不到token的时候就往redis里面存一个
                .get(String.format(DEVICE_TOKEN, deviceId, username))
                .switchIfEmpty(createNewToken(deviceId, username))
                .flatMap(e -> {
                    IDevice device = Device.builder().accessToken(e).deviceId(finalDeviceId).username(username)
                            .name(name).build();
                    return Mono.just(device);
                });

    }

    private Mono<String> createNewToken(String deviceId, String username) {
        String token = UUID.randomUUID().toString();
        Duration duration = Duration.ofDays(1L);
        Mono<Boolean> saveOps1 = redisTemplate.opsForValue().set(String.format(DEVICE_TOKEN, deviceId, username), token, duration);
        Map<String, String> userInfo = Map.of("username", username, "device_id", deviceId);
        Mono<Boolean> expireOps = redisTemplate.expire(String.format(TOKEN_USER, token), duration);
        Mono<Boolean> saveOps2 = redisTemplate.opsForHash().putAll(String.format(TOKEN_USER, token), userInfo).then(expireOps);
        return Mono.zip(saveOps1, saveOps2).map(e -> token);
    }


    public Mono<IDevice> find(String token) {
        Map<String, String> tempMap = new ConcurrentHashMap<>(8);
        return redisTemplate.opsForHash().entries(String.format(TOKEN_USER, token))
                .doOnNext(o -> {
                    tempMap.put(o.getKey().toString(), o.getValue().toString());
                })
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)))
                .last()
                .map(e ->
                        Device.builder()
                                .username(tempMap.get("username"))
                                .deviceId(tempMap.get("device_id"))
                                .accessToken(token)
                                .build()
                );
    }
}
