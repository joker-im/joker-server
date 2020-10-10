package im.joker.api.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
public class LoginRequest {

    @NotNull(message = "登录类型不可为空")
    private String type;

    private UserIdentifier identifier;

    private String password;

    private String deviceId;

    private String initialDeviceDisplayName;


    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class UserIdentifier {
        private String type;
        private String user;
    }
}
