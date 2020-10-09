package im.joker.event.room;

import org.apache.commons.lang3.StringUtils;

/**
 * 房间消息类型的msgtype的类型。
 *
 * @author linyurong
 */
public enum MessageType {
    /**
     * e.g.:
     * <pre>
     *      "content": {
     *         "body": "This is an example text message",
     *         "msgtype": "m.text"
     *     },
     * </pre>
     */
    TEXT("m.text"),
    /**
     * e.g.:
     * <pre>
     *     "content": {
     *         "body": "filename.jpg",
     *         "info": {
     *             "h": 398,
     *             "mimetype": "image/jpeg",
     *             "size": 31037,
     *             "w": 394
     *         },
     *         "msgtype": "m.image",
     *         "url": "mxc://localhost/JWEIFJgwEIhweiWJE"
     *     },
     * </pre>
     */
    IMAGE("m.image"),
    AUDIO("m.audio"),
    VIDEO("m.video");

    private String type;

    MessageType(String type) {
        this.type = type;
    }

    public String get() {
        return type;
    }

    public boolean is(String type) {
        return StringUtils.equals(this.type, type);
    }
}
