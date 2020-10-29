package im.joker.api.vo.account;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RegisterRequest {

    @NotNull(message = "用户账号不可为空")
    private String username;

    @NotBlank
    @Length(min = 6, max = 32, message = "密码长度必须在6-32位以内")
    private String password;

    private String deviceId;

    private String initialDeviceDisplayName;


}
