package im.joker.api.vo.key;

import java.util.Map;

public class KeysUploadRequest {

    private DeviceKey deviceKeys;

    private Map<String, Object> oneTimeKeys;

    public DeviceKey getDeviceKeys() {
        return deviceKeys;
    }

    public void setDeviceKeys(DeviceKey deviceKeys) {
        this.deviceKeys = deviceKeys;
    }

    public Map<String, Object> getOneTimeKeys() {
        return oneTimeKeys;
    }

    public void setOneTimeKeys(Map<String, Object> oneTimeKeys) {
        this.oneTimeKeys = oneTimeKeys;
    }


}