package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 11:24
 * @Desc:
 */
public class RoomJoinRuleContent extends AbstractStateContent {

    // ["public", "knock", "invite", "private"] 只能为其一
    private String joinRule;

    public String getJoinRule() {
        return joinRule;
    }

    public void setJoinRule(String joinRule) {
        this.joinRule = joinRule;
    }
}
