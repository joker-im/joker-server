package im.joker.store;

import im.joker.event.room.IRoomEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import reactor.core.publisher.Mono;

/**
 * @author linyurong
 * @date 2020/9/30 16:33
 */
public interface IStore {

    void addRoom(IRoom room);

    IRoomEvent addEvent(IRoomEvent event);

    Mono<IUser> addUser(IUser user);

    Mono<IUser> retrieveById(String id);

    Mono<IUser> retrieveByUsername(String username);
}
