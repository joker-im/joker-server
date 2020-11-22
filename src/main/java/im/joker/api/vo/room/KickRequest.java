package im.joker.api.vo.room;

import javax.validation.constraints.NotNull;

/**
 * @Author: mkCen
 * @Date: 2020/11/2
 * @Time: 23:42
 * @Desc:
 */
public class KickRequest {

    @NotNull(message = "userId不可为空")
    private String userId;

    private String reason;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
