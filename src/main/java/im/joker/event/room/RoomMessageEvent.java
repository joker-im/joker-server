package im.joker.event.room;

import im.joker.event.EventType;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 消息事件。
 *
 * @author linyurong
 */
public class RoomMessageEvent extends AbstractRoomEvent<HashMap> {

    public static final String MSGTYPE = "msgtype";
    public static final String BODY = "body";

    public RoomMessageEvent() {}

    public RoomMessageEvent(String userId, HashMap<String, Serializable> content, String transactionId) {
        super(EventType.Message.get(), userId, System.currentTimeMillis(), content);
        setTransactionId(transactionId);
    }

    public RoomMessageEvent(String sender, String msgtype, String body, String transactionId) {
        super(EventType.Message.get(), sender, System.currentTimeMillis());
        setTransactionId(transactionId);
        HashMap<String, Serializable> simpleContent = new HashMap<>();
        simpleContent.put(MSGTYPE, msgtype);
        simpleContent.put(BODY, body);

        setContent(simpleContent);
    }

    public RoomMessageEvent(String sender, String body, String transactionId) {
        this(sender, MessageType.TEXT.get(), body, transactionId);
    }
}
