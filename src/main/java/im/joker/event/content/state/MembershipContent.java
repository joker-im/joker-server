package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;

import javax.validation.constraints.NotNull;

/**
 * 就是文档的EventContent类型
 */
public class MembershipContent extends AbstractStateContent {

    private String avatarUrl;

    @JsonProperty("displayname")
    private String displayName;


    /**
     * ["invite", "join", "knock", "leave", "ban"] 必须为这其一
     */
    @NotNull
    private String membership;


    private Boolean isDirect;


    private String reason;

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

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public Boolean getDirect() {
        return isDirect;
    }

    public void setDirect(Boolean direct) {
        isDirect = direct;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
