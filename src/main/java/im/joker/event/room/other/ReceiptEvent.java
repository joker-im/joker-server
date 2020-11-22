package im.joker.event.room.other;

import im.joker.event.content.IContent;
import im.joker.event.content.other.ReceiptContent;
import im.joker.event.room.AbstractRoomEvent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:22
 * @Desc:
 */
public class ReceiptEvent extends AbstractRoomEvent {


    private ReceiptContent content;


    @Override
    public IContent getContent() {
        return content;
    }

    public void setContent(ReceiptContent content) {
        this.content = content;
    }
}
