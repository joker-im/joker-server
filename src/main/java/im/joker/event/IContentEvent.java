package im.joker.event;

/**
 * 包含一个<code>content</code>字段的基本事件。
 *
 * @author linyurong
 */
public interface IContentEvent<T> {
    /**
     * 只有被持久化保存的事件才有的一个全局唯一，并且递增的streamId。
     *
     * @return streamId
     */
    Long getStreamId();

    void setStreamId(Long streamId);

    /**
     * 每个事件都有的唯一标识。不管事件有没有被保存。
     * @return evanId
     */
    String getEventId();

    /**
     * 事件的类型。
     *
     * @return type
     */
    String getType();

    /**
     * 事件的发送者。
     *
     * @return sender
     */
    String getSender();

    /**
     * 事件生成的时间，为服务器端的事件。
     *
     * @return server timestamp
     */
    Long getOriginServerTs();

    /**
     * 事件的事务id，用于防止重复提交同一事件(如在重试时)。
     *
     * @return transaction id to prevent duplicate
     */
    String getTransactionId();

    /**
     * 事件的主要内容。
     *
     * @return main content
     */
    T getContent();
}
