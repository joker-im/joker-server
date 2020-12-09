package im.joker.event.room.other;

import im.joker.event.content.other.ReceiptContent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 0:22
 * @Desc:
 */
public class ReceiptEvent extends EphemeralEvent {


    private ReceiptContent content;


    @Override
    public ReceiptContent getContent() {
        return content;
    }

    public void setContent(ReceiptContent content) {
        this.content = content;
    }
}
