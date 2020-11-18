package im.joker.helper;

import im.joker.api.vo.sync.SyncResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonReactiveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static im.joker.constants.ImContents.SYNC_DEVICE_TOPIC;

@Component
@Slf4j
public class LongPollingHelper {


    @Autowired
    private RedissonReactiveClient redissonReactiveClient;

    /**
     * sync请求等待响应列表,key是DeviceId, value是monoSink
     */
    private final Map<String, MonoSink<Boolean>> waitingDeviceMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void subscribeNotifySyncMessage() {
        redissonReactiveClient.getTopic(SYNC_DEVICE_TOPIC)
                .addListener(String.class, (channel, deviceId) -> {
                    log.debug("收到channel:{},deviceId:{} sync完成的消息", channel, deviceId);
                    if (waitingDeviceMap.containsKey(deviceId)) {
                        MonoSink<Boolean> monoSink = waitingDeviceMap.get(deviceId);
                        monoSink.success(true);
                    }
                }).subscribe();
    }

    /**
     * 唤醒正在同步的长轮询设备
     *
     * @param deviceId
     */
    public Mono<Long> notifySyncDevice(String deviceId) {
        return redissonReactiveClient.getTopic(SYNC_DEVICE_TOPIC).publish(deviceId);
    }

    /**
     * 长沦陷过程中需要添加同步设备,以阻塞
     *
     * @param deviceId
     * @param monoSink
     */

    public void addSyncDevice(String deviceId, MonoSink<Boolean> monoSink) {
        waitingDeviceMap.put(deviceId, monoSink);
    }


    /**
     * 删除对应的monoSink
     *
     * @param deviceId
     */
    public void removeSyncDevice(String deviceId) {
        waitingDeviceMap.remove(deviceId);
    }


}
