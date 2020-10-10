package im.joker.device;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

import static im.joker.constants.ImRedisKeys.DEVICE_TOKEN;

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
        return redisTemplate.opsForValue().set(String.format(DEVICE_TOKEN, deviceId, username), token, Duration.ofDays(1L))
                .map(e -> token);
    }


}
