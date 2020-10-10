package im.joker.room;

import im.joker.event.IContentEvent;

/**
 * @author linyurong
 * @date 2020/9/30 16:46
 */
public interface IRoom {

    /**
     * 房间注入一个事件,返回eventId
     *
     * @param ev
     * @return eventId
     */
    String inject(IContentEvent ev);


    /**
     * 获取房间最新状态。
     *
     * @return
     */
    IRoomState getCurrentState();


    /**
     * 获取某事件下的房间状态。
     *
     * @param eventId
     * @return
     */
    IRoomState getStateFor(String eventId);


}
