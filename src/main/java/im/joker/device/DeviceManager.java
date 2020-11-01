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
import java.util.stream.Collectors;

import static im.joker.constants.ImRedisKeys.*;

@Component
public class DeviceManager {


    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;


    /**
     * 在redis中查询是否存在accessToken,存在则返回
     *
     * @param deviceId
     * @param username
     * @param deviceName
     * @return
     */
    public Mono<IDevice> findOrCreateDevice(String deviceId, String username, String userId, @Nullable String deviceName) {
        String redisTokenMapKey = String.format(USER_DEVICES_TOKENS_HASH, username);
        return redisTemplate.opsForHash()
                // redis中找不到token的时候就往redis里面存一个
                .hasKey(redisTokenMapKey, deviceId)
                .flatMap(e -> {
                    if (!e) {
                        return createNewToken(deviceId, username, deviceName, userId);
                    } else {
                        // 如果找到了,还要比对2个键上的token是否一样(因为USER_DEVICE_TOKEN_HASH不会过期)
                        return redisTemplate.opsForHash().get(redisTokenMapKey, deviceId)
                                .zipWhen(oldToken -> redisTemplate.hasKey(String.format(TOKEN_USER_HASH, oldToken)))
                                .flatMap(tuple2 -> {
                                    // 如果在TOKEN_USER 不存在,只能新创建key
                                    if (!tuple2.getT2()) {
                                        return createNewToken(deviceId, username, deviceName, userId);
                                    } else {
                                        return Mono.just(tuple2.getT1().toString());
                                    }
                                });
                    }
                })
                .map(e -> Device.builder().accessToken(e).deviceId(deviceId).username(username)
                        .name(deviceName).userId(userId).build());

    }

    private Mono<String> createNewToken(String deviceId, String username, String deviceName, String userId) {
        String token = UUID.randomUUID().toString();
        Duration duration = Duration.ofDays(1L);
        Mono<Boolean> saveOps1 = redisTemplate.opsForHash().put(String.format(USER_DEVICES_TOKENS_HASH, username), deviceId, token);
        Map<String, String> userInfoMap = Map.of(TOKEN_USER_HASH_KEY_USERNAME, username,
                TOKEN_USER_HASH_KEY_DEVICE_ID, deviceId,
                TOKEN_USER_HASH_HASH_KEY_USER_ID, userId,
                TOKEN_USER_HASH_HASH_KEY_DEVICE_NAME, deviceName);
        Mono<Boolean> expireOps = redisTemplate.expire(String.format(TOKEN_USER_HASH, token), duration);
        Mono<Boolean> expireOps2 = redisTemplate.expire(String.format(USER_DEVICES_TOKENS_HASH, username), duration);
        Mono<Void> saveOps2 = redisTemplate.opsForHash().putAll(String.format(TOKEN_USER_HASH, token), userInfoMap).then();
        return Mono.zip(saveOps1, saveOps2).then(Mono.zip(expireOps, expireOps2)).flatMap(e -> Mono.just(token));
    }


    public Mono<IDevice> find(String token) {
        return redisTemplate.opsForHash().entries(String.format(TOKEN_USER_HASH, token))
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)))
                .collect(Collectors.toMap(e -> e.getKey().toString(), e -> e.getValue().toString()))
                .map(e ->
                        Device.builder()
                                .username(e.get(TOKEN_USER_HASH_KEY_USERNAME))
                                .deviceId(e.get(TOKEN_USER_HASH_KEY_DEVICE_ID))
                                .userId(e.get(TOKEN_USER_HASH_HASH_KEY_USER_ID))
                                .name(e.get(TOKEN_USER_HASH_HASH_KEY_DEVICE_NAME))
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

    public Mono<Void> deleteAllDevices(String username) {
        return redisTemplate.delete(String.format(USER_DEVICES_TOKENS_HASH, username)).then();
    }

    public Flux<IDevice> findDevices(String username) {
        Flux<Map.Entry<Object, Object>> entries = redisTemplate.opsForHash().entries(String.format(USER_DEVICES_TOKENS_HASH, username));
        return entries.map(e -> Device.builder().accessToken(e.getValue().toString()).username(username).deviceId(e.getKey().toString()).build());
    }


}
