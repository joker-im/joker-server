package im.joker.device;

import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static im.joker.constants.ImRedisKeys.USER_DEVICES_TOKENS_HASH;
import static im.joker.constants.ImRedisKeys.TOKEN_USER_HASH;

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
    public Mono<IDevice> findOrCreateDevice(String deviceId, String username, String userId, @Nullable String name) {
        String redisTokenMapKey = String.format(USER_DEVICES_TOKENS_HASH, username);
        return redisTemplate.opsForHash()
                // redis中找不到token的时候就往redis里面存一个
                .hasKey(redisTokenMapKey, deviceId)
                .flatMap(e -> {
                    if (!e) {
                        return createNewToken(deviceId, username, userId);
                    } else {
                        // 如果找到了,还要比对2个键上的token是否一样(因为USER_DEVICE_TOKEN_HASH不会过期)
                        return redisTemplate.opsForHash().get(redisTokenMapKey, deviceId)
                                .zipWhen(oldToken -> redisTemplate.hasKey(String.format(TOKEN_USER_HASH, oldToken)))
                                .flatMap(tuple2 -> {
                                    // 如果在TOKEN_USER 不存在,只能新创建key
                                    if (!tuple2.getT2()) {
                                        return createNewToken(deviceId, username, userId);
                                    } else {
                                        return Mono.just(tuple2.getT1().toString());
                                    }
                                });
                    }
                })
                .map(e -> Device.builder().accessToken(e).deviceId(deviceId).username(username)
                        .name(name).userId(userId).build());

    }

    private Mono<String> createNewToken(String deviceId, String username, String userId) {
        String token = UUID.randomUUID().toString();
        Duration duration = Duration.ofDays(1L);
        Mono<Boolean> saveOps1 = redisTemplate.opsForHash().put(String.format(USER_DEVICES_TOKENS_HASH, username), deviceId, token);
        Map<String, String> userInfo = Map.of("username", username, "device_id", deviceId, "user_id", userId);
        Mono<Boolean> expireOps = redisTemplate.expire(String.format(TOKEN_USER_HASH, token), duration);
        Mono<Boolean> saveOps2 = redisTemplate.opsForHash().putAll(String.format(TOKEN_USER_HASH, token), userInfo).then(expireOps);
        return Mono.zip(saveOps1, saveOps2).map(e -> token);
    }


    public Mono<IDevice> find(String token) {
        Map<String, String> tempMap = new ConcurrentHashMap<>(8);
        return redisTemplate.opsForHash().entries(String.format(TOKEN_USER_HASH, token))
                .doOnNext(o -> tempMap.put(o.getKey().toString(), o.getValue().toString()))
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)))
                .last()
                .map(e ->
                        Device.builder()
                                .username(tempMap.get("username"))
                                .deviceId(tempMap.get("device_id"))
                                .userId(tempMap.get("user_id"))
                                .accessToken(token)
                                .build()
                );
    }

    public Mono<Void> removeDevice(IDevice e) {
        String tokenUser = String.format(TOKEN_USER_HASH, e.getAccessToken());
        Mono<Long> deleteOps1 = redisTemplate.opsForHash().remove(String.format(USER_DEVICES_TOKENS_HASH, e.getUsername()), e.getDeviceId());
        Mono<Long> deleteOps2 = redisTemplate.delete(tokenUser);
        return Mono.zip(deleteOps1, deleteOps2).then();
    }

    public Flux<IDevice> findDevices(String username) {
        Flux<Map.Entry<Object, Object>> entries = redisTemplate.opsForHash().entries(String.format(USER_DEVICES_TOKENS_HASH, username));
        return entries.map(e -> Device.builder().accessToken(e.getValue().toString()).deviceId(e.getKey().toString()).build());
    }


}
