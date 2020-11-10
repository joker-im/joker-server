package im.joker.room;

import im.joker.event.ImEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

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
    Mono<ImEvent> injectEvent(ImEvent ev);


    Flux<ImEvent> injectEvents(List<ImEvent> evs);

    /**
     * 获取房间最新状态。
     *
     * @return
     */
    RoomState getCurrentState();


    /**
     * 获取某事件下的房间状态。
     *
     * @param eventId
     * @return
     */
    RoomState getStateFor(String eventId);


    /**
     * 获取roomId
     *
     * @return
     */
    String getRoomId();


}
