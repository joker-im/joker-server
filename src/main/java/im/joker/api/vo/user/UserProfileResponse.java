package im.joker.api.vo.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @Author: mkCen
 * @Date: 2020/11/26
 * @Time: 21:53
 * @Desc:
 */
public class UserProfileResponse {


    private String avatarUrl;

    @JsonProperty("displayname")
    private String displayName;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
