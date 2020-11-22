package im.joker.event;

import im.joker.event.content.IContent;
import im.joker.event.content.message.*;

/**
 * 房间消息类型的msgType的类型。
 *
 * @author linyurong
 */
public enum MessageType {


    TEXT("m.text", CommonTextContent.class),
    EMOTE("m.emote", CommonTextContent.class),
    NOTICE("m.notice", CommonTextContent.class),
    LOCATION("m.location", LocationContent.class),
    IMAGE("m.image", ImageContent.class),
    AUDIO("m.audio", AudioContent.class),
    VIDEO("m.video", VideoContent.class),
    FILE("m.file", FileContent.class);

    private final String id;

    private final Class<? extends IContent> contentClass;

    MessageType(String id, Class<? extends IContent> clazz) {
        this.id = id;
        this.contentClass = clazz;
    }

    public String getId() {
        return id;
    }

    public Class<? extends IContent> getContentClass() {
        return contentClass;
    }

}
