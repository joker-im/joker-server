package im.joker.event.room.other;

import im.joker.event.content.other.FullReadContent;
import im.joker.event.room.AbstractRoomEvent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 10:08
 * @Desc:
 */
public class FullReadMarkerEvent extends AbstractRoomEvent {


    private FullReadContent content;

    @Override
    public FullReadContent getContent() {
        return content;
    }

    public void setContent(FullReadContent content) {
        this.content = content;
    }
}
