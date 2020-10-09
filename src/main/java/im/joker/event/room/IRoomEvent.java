package im.joker.event.room;

import im.joker.event.IContentEvent;

/**
 * 房间事件。
 *
 * @author linyurong
 **/
public interface IRoomEvent<T> extends IContentEvent<T> {
    /**
     * 房间id。
     *
     * @return roomId
     */
    String getRoomId();

    /**
     * 状态的key, 同一种状态和同一个key在房间内只有一个最新的事件。
     *
     * @return stateKey
     */
    String getStateKey();
}
