package im.joker.room;

import im.joker.device.IDevice;
import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author linyurong
 * @date 2020/9/30 16:46
 */
public interface IRoom {

    /**
     * 房间注入一个事件
     *
     * @param ev
     * @return eventId
     */
    Mono<AbstractRoomEvent> injectEvent(AbstractRoomEvent ev, IDevice device);

    Flux<AbstractRoomEvent> injectEvents(List<AbstractRoomEvent> evs, IDevice device);

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
