package im.joker.api.vo.account;


import javax.validation.constraints.NotNull;

public class LoginRequest {

    @NotNull(message = "登录类型不可为空")
    private String type;

    private UserIdentifier identifier;

    private String password;

    private String deviceId;

    private String initialDeviceDisplayName;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public UserIdentifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(UserIdentifier identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getInitialDeviceDisplayName() {
        return initialDeviceDisplayName;
    }

    public void setInitialDeviceDisplayName(String initialDeviceDisplayName) {
        this.initialDeviceDisplayName = initialDeviceDisplayName;
    }

    public static class UserIdentifier {
        private String type;
        private String user;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
