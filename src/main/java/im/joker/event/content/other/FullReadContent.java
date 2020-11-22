package im.joker.event.content.other;

import im.joker.event.content.IContent;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 10:09
 * @Desc:
 */
public class FullReadContent implements IContent {

    private String eventId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
