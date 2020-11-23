package im.joker.event.room.other;

import im.joker.event.content.other.TypingContent;
import im.joker.event.room.AbstractRoomEvent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:14
 * @Desc:
 */
public class TypingEvent extends AbstractRoomEvent {


    private TypingContent content;


    @Override
    public TypingContent getContent() {
        return content;
    }

    public void setContent(TypingContent content) {
        this.content = content;
    }
}
