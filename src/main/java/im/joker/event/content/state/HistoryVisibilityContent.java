package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
public class HistoryVisibilityContent extends AbstractStateContent {
    /**
     * 该值为 ["invited", "joined", "shared", "world_readable"] 任意一种
     * 当是invited时, 用户被邀请的那一刻开始都能够访问那之后的事件
     * 当是joined时, 用户加入了房间之后才能看到
     * 当是shared时, 用户join到房间后可以看到该房间的所有事件. 甚至是之前的还没进去的时候
     * world_readable时, 没加入的人也能看到所有的聊天数据
     */
    private String historyVisibility;

    public String getHistoryVisibility() {
        return historyVisibility;
    }

    public void setHistoryVisibility(String historyVisibility) {
        this.historyVisibility = historyVisibility;
    }
}
