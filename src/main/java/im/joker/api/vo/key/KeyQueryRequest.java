package im.joker.api.vo.key;

public class KeyQueryRequest {

    private Long timeout;

    private DeviceKey deviceKeys;

    private String token;


    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public DeviceKey getDeviceKeys() {
        return deviceKeys;
    }

    public void setDeviceKeys(DeviceKey deviceKeys) {
        this.deviceKeys = deviceKeys;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
