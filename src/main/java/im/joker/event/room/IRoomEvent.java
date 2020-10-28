package im.joker.event.room;

import im.joker.event.ImEvent;

/**
 * 房间事件。
 *
 * @author linyurong
 **/
public interface IRoomEvent extends ImEvent {
    /**
     * 房间id。
     *
     * @return roomId
     */
    String getRoomId();

    /**
     * 事件id
     *
     * @return eventId
     */
    String getEventId();


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
     * 可以包含一些额外信息
     *
     * @return UnsignedData
     */
    UnsignedData getUnsigned();

}
