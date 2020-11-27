package im.joker.api.vo.key;

import java.util.List;
import java.util.Map;

public class DeviceKey {

    private String userId;

    private String deviceId;

    private List<String> algorithms;

    private Map<String, Object> keys;

    private Map<String, Object> signatures;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getAlgorithms() {
        return algorithms;
    }

    public void setAlgorithms(List<String> algorithms) {
        this.algorithms = algorithms;
    }

    public Map<String, Object> getKeys() {
        return keys;
    }

    public void setKeys(Map<String, Object> keys) {
        this.keys = keys;
    }

    public Map<String, Object> getSignatures() {
        return signatures;
    }

    public void setSignatures(Map<String, Object> signatures) {
        this.signatures = signatures;
    }
}
