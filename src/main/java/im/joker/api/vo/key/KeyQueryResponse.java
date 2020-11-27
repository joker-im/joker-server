package im.joker.api.vo.key;

import java.util.Map;

public class KeyQueryResponse {

    private Map<String,Object> failures;

    private DeviceKey deviceKeys;

    public Map<String, Object> getFailures() {
        return failures;
    }

    public void setFailures(Map<String, Object> failures) {
        this.failures = failures;
    }

    public DeviceKey getDeviceKeys() {
        return deviceKeys;
    }

    public void setDeviceKeys(DeviceKey deviceKeys) {
        this.deviceKeys = deviceKeys;
    }
}
