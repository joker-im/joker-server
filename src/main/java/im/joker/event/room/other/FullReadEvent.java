package im.joker.event.room.other;

import im.joker.event.content.IContent;
import im.joker.event.content.other.FullReadContent;
import im.joker.event.room.AbstractRoomEvent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 10:08
 * @Desc:
 */
public class FullReadEvent extends AbstractRoomEvent {


    private FullReadContent content;

    @Override
    public IContent getContent() {
        return content;
    }

    public void setContent(FullReadContent content) {
        this.content = content;
    }
}
