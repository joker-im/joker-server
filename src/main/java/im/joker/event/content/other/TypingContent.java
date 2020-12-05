package im.joker.event.content.other;

import im.joker.event.content.IContent;

import java.util.List;
import java.util.Set;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:17
 * @Desc:
 */
public class TypingContent implements IContent {

    private Set<String> userIds;

    private Integer timeout;

    private Boolean typing;


    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }

    public Set<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<String> userIds) {
        this.userIds = userIds;
    }
}
