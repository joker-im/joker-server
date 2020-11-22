package im.joker.api.vo.room;

import javax.validation.constraints.NotNull;
public class InviteRequest {

    @NotNull(message = "userId不可为空")
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
