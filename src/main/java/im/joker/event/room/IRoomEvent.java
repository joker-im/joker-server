package im.joker.event.room;

import im.joker.event.ImEvent;

public interface IRoomEvent extends ImEvent {

    /**
     * 房间id
     *
     * @return
     */
    String getRoomId();

    /**
     * 事件id
     *
     * @return
     */
    String getEventId();


    /**
     * 发送事件
     *
     * @return
     */
    Long getOriginServerTs();

    /**
     * 事务id
     *
     * @return
     */
    String getTransactionId();

    /**
     * 一些附加事件
     *
     * @return
     */
    UnsignedData getUnsigned();

    /**
     * 发送者
     *
     * @return
     */
    String getSender();

    /**
     * 时序id
     *
     * @return
     */
    Long getStreamId();
}
