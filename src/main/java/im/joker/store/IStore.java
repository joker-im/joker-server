package im.joker.store;

import im.joker.event.EventType;
import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.room.IRoom;
import im.joker.user.IUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
    Mono<AbstractRoomEvent> addEvent(AbstractRoomEvent event);


    Flux<AbstractRoomEvent> addEvents(List<AbstractRoomEvent> events);

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
    Mono<IUser> findUserByUserId(String id);

    /**
     * 检索用户
     *
     * @param username
     * @return
     */
    Mono<IUser> findUserByUsername(String username);


    /**
     * 通过userId查询指定房间
     *
     * @param userId
     * @return
     */
    Flux<IRoom> findRoomByUserId(String userId);

    /**
     * 查询某个用户指定的事件类型
     *
     * @param eventType
     * @param userId
     * @return
     */
    Flux<AbstractRoomEvent> findEvents(EventType eventType, String userId);

    /**
     * 查询指定房间的状态事件
     *
     * @param roomId
     * @return
     */
    Flux<AbstractRoomStateEvent> findRoomStateEvents(String roomId);

    /**
     * 查询房间
     *
     * @param targetRoomId
     * @return
     */
    Mono<IRoom> findRoomByRoomId(String targetRoomId);

    Flux<AbstractRoomStateEvent> findRoomStateEvents(List<String> roomIds);
}
