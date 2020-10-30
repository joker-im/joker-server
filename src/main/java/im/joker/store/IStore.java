package im.joker.store;

import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import reactor.core.publisher.Mono;

/**
 * @author linyurong
 * @date 2020/9/30 16:33
 */
public interface IStore {

    /**
     * 插入房间
     *
     * @param room
     * @return
     */
    Mono<IRoom> addRoom(IRoom room);

    /**
     * 插入事件
     *
     * @param event
     * @return
     */
    Mono<ImEvent> addEvent(ImEvent event);


    /**
     * 添加用户
     *
     * @param user
     * @return
     */
    Mono<IUser> addUser(IUser user);

    /**
     * 检索用户
     *
     * @param id
     * @return
     */
    Mono<IUser> retrieveById(String id);

    /**
     * 检索用户
     *
     * @param username
     * @return
     */
    Mono<IUser> retrieveByUsername(String username);
}
