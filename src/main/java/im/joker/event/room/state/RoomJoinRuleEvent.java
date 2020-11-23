package im.joker.event.room.state;

import im.joker.event.content.state.RoomJoinRuleContent;
import im.joker.event.room.AbstractRoomStateEvent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 11:21
 * @Desc:
 */

public class RoomJoinRuleEvent extends AbstractRoomStateEvent {

    private RoomJoinRuleContent content;

    @Override
    public RoomJoinRuleContent getContent() {
        return content;
    }


    public void setContent(RoomJoinRuleContent content) {
        this.content = content;
    }
}
