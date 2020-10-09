package im.joker.store;

import im.joker.event.room.IRoomEvent;
import im.joker.room.IRoom;

/**
 * @author linyurong
 * @date 2020/9/30 16:33
 */
public interface IStore {

    void addRoom(IRoom room);

    IRoomEvent addEvent(IRoomEvent event);
}
