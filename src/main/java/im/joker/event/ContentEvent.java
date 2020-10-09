package im.joker.event;

import im.joker.exception.InvalidJsonException;
import im.joker.util.GsonUtils;

import java.util.Map;

/**
 * @author linyurong
 */
public abstract class ContentEvent<T> implements IContentEvent<T> {
    protected Long streamId;
    protected String eventId;
    protected String type;
    protected String sender;
    protected long originServerTs;
    protected String transactionId;
    protected T content;

    public ContentEvent() {}

    public ContentEvent(String type, String sender, Long originServerTs) {
        this.type = type;
        this.sender = sender;
        this.originServerTs = originServerTs;
    }

    public ContentEvent(String type, String sender, Long originServerTs, T content) {
        this(type, sender, originServerTs);
        this.content = content;
    }

    /**
     * 根据事件的类型来转换成具体的子类型事件。
     *
     * @param streamId 被保存的streamId
     * @param rawJson json
     * @return IContentEvent的实现子类
     */
    public static IContentEvent from(Long streamId, String rawJson) {
        IContentEvent event = from(rawJson);
        event.setStreamId(streamId);
        return event;
    }

    /**
     * 根据事件的类型来转换成具体的子类型事件。
     *
     * @param rawJson json
     * @return IContentEvent的实现子类
     */
    public static IContentEvent from(String rawJson) {
        Map map = GsonUtils.parseMap(rawJson);
        if (!map.containsKey(EventKey.Type.get()) || !map.containsKey(EventKey.Sender.get())) {
            throw new InvalidJsonException("Not a correct event: " + rawJson);
        }

        String type = map.get(EventKey.Type.get()).toString();
        IContentEvent specificEvent = EventType.convertToSpecificEvent(type, rawJson);
        if (specificEvent == null) {
            throw new InvalidJsonException(String.format("Not a recognized event type %s: %s", type, rawJson));
        }

        return specificEvent;
    }

    @Override
    public Long getStreamId() {
        return streamId;
    }

    @Override
    public void setStreamId(Long streamId) {
        this.streamId = streamId;
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    private void setProperties(IContentEvent<T> o) {
        this.streamId = o.getStreamId();
        this.type = o.getType();
        this.sender = o.getSender();
        this.originServerTs = o.getOriginServerTs();
        this.content = o.getContent();

        if (o instanceof ContentEvent) {
            ContentEvent o1 = (ContentEvent) o;
            this.transactionId = o1.transactionId;
        }
    }

    @Override
    public String getTransactionId() {
        return transactionId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setOriginServerTs(long originServerTs) {
        this.originServerTs = originServerTs;
    }

    @Override
    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String getSender() {
        return sender;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Long getOriginServerTs() {
        return originServerTs;
    }

    @Override
    public String toString() {
        return this.getClass() + ": " + GsonUtils.get().toJson(this);
    }
}
