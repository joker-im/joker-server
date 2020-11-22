package im.joker.event.content.other;

import im.joker.event.content.IContent;

import java.util.List;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:17
 * @Desc:
 */
public class TypingContent implements IContent {

    private List<String> userIds;

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
}
