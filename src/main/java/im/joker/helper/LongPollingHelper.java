package im.joker.helper;

import org.springframework.stereotype.Component;
import reactor.core.publisher.MonoSink;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LongPollingHelper {


    /**
     * sync请求等待响应列表,key是DeviceId, value是monoSink
     */
    private final Map<String, MonoSink<Boolean>> waitingDeviceMap = new ConcurrentHashMap<>();


    /**
     * 唤醒正在同步的长轮询设备
     *
     * @param deviceId
     */
    public void notifySyncDevice(String deviceId) {
        if (waitingDeviceMap.containsKey(deviceId)) {
            MonoSink<Boolean> monoSink = waitingDeviceMap.get(deviceId);
            monoSink.success(true);
        }
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
