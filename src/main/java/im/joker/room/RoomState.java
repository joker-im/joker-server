package im.joker.room;

import im.joker.event.EventType;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.event.room.state.PowerLevelEvent;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.GlobalStateHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Author: mkCen
 * @Date: 2020/11/1
 * @Time: 0:44
 * @Desc:
 */
@Slf4j
public class RoomState {

    /**
     * 此变量表示该房间的所有状态事件
     */
    private final List<AbstractRoomStateEvent> stateEvents;


    private final IRoom room;

    /**
     * 此变量代表该房间最新的状态事件, 其key是 eventType + stateKey
     */
    private final Map<String, AbstractRoomStateEvent> stateEventsMap;


    public IRoom getRoom() {
        return room;
    }

    public List<AbstractRoomStateEvent> getStateEvents() {
        return stateEvents;
    }


    public List<AbstractRoomStateEvent> distinctStateEvents() {
        return stateEventsMap.values().stream().sorted(Comparator.comparing(AbstractRoomEvent::getStreamId)).collect(Collectors.toList());
    }


    public AbstractRoomStateEvent getEarliestRoomStateEvent() {
        return stateEvents.get(stateEvents.size() - 1);
    }


    /**
     * 残缺的roomState,从状态事件List中获取roomState,是一个
     *
     * @param list
     * @return
     */
    public static RoomState from(List<AbstractRoomStateEvent> list) {
        List<AbstractRoomStateEvent> stateEvents = list.stream().sorted(Comparator.comparing(AbstractRoomEvent::getStreamId).reversed()).collect(Collectors.toList());
        return new RoomState(null, null, stateEvents, handleStateEvents(stateEvents));
    }


    private static Map<String, AbstractRoomStateEvent> handleStateEvents(List<AbstractRoomStateEvent> list) {
        return list.stream().collect(Collectors.toMap(e -> e.getType() + e.getStateKey(), e -> e, (o, n) -> {
            if (o.getStreamId() > n.getStreamId()) {
                return o;
            }
            return n;
        }));
    }


    public MembershipEvent searchMembershipEvent(String userId) {
        AbstractRoomStateEvent event = stateEventsMap.get(EventType.Membership.getId() + userId);
        if (event != null) {
            return (MembershipEvent) event;
        }
        return null;
    }

    /**
     * 从roomId获取最新的房间状态
     *
     * @param roomId
     * @param globalStateHolder
     * @return
     */
    public static Mono<RoomState> getRoomState(String roomId, GlobalStateHolder globalStateHolder) {
        Flux<AbstractRoomStateEvent> eventFlux = globalStateHolder.getMongodbStore().findRoomStateEvents(roomId);
        Mono<IRoom> roomMono = globalStateHolder.getMongodbStore().findRoomByRoomId(roomId);
        return eventFlux
                // 逆序
                .collectSortedList(Comparator.comparing(AbstractRoomEvent::getStreamId).reversed())
                .zipWith(roomMono)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "房间不存在")))
                .map(tuple2 -> {
                    Map<String, AbstractRoomStateEvent> userStateEventMap = handleStateEvents(tuple2.getT1());
                    return new RoomState(globalStateHolder, tuple2.getT2(), tuple2.getT1(), userStateEventMap);
                });
    }

    private RoomState(GlobalStateHolder globalStateHolder, IRoom room, List<AbstractRoomStateEvent> stateEvents,
                      Map<String, AbstractRoomStateEvent> stateEventsMap) {
        this.room = room;
        this.stateEvents = stateEvents;
        this.stateEventsMap = stateEventsMap;
        if (room != null) {
            ((Room) room).setGlobalStateHolder(globalStateHolder);
        }
    }

    public PowerLevelEvent findRoomPowerLevelEvent() {
        AbstractRoomStateEvent event = stateEventsMap.get(EventType.PowerLevel.getId() + "");
        if (event != null) {
            return (PowerLevelEvent) event;
        }
        return null;
    }
}
