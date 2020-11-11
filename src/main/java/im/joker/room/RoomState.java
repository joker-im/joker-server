package im.joker.room;

import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.GlobalStateHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
@Getter
public class RoomState {

    private final GlobalStateHolder globalStateHolder;
    /**
     * 此变量表示该房间的所有状态事件
     */
    private final List<AbstractRoomStateEvent> stateEvents;

    /**
     * 此变量表示该房间所有人的memberEvent. 其key是stateKey, 存储最新的一条membership
     */
    private final Map<String, MembershipEvent> latestMembershipEventMap;

    private final IRoom room;


    public static Mono<RoomState> existRoomState(String roomId, GlobalStateHolder globalStateHolder) {
        Flux<ImEvent> eventFlux = globalStateHolder.getMongodbStore().findRoomStateEvents(roomId);
        Mono<IRoom> roomMono = globalStateHolder.getMongodbStore().findRoomByRoomId(roomId);
        return eventFlux
                .map(e -> (AbstractRoomStateEvent) e)
                // 逆序
                .collectSortedList((a, b) -> b.getStreamId().compareTo(a.getStreamId()))
                .zipWith(roomMono)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "房间不存在")))
                .map(tuple2 -> {
                    Map<String, MembershipEvent> userStateEventMap = tuple2.getT1().stream()
                            .filter(e -> e instanceof MembershipEvent)
                            .map(e -> (MembershipEvent) e)
                            .collect(Collectors.toMap(AbstractRoomStateEvent::getStateKey, e -> e, (o, n) -> {
                                if (o.getStreamId().compareTo(n.getStreamId()) > 0) {
                                    return o;
                                } else {
                                    return n;
                                }
                            }));
                    return new RoomState(globalStateHolder, tuple2.getT2(), tuple2.getT1(), userStateEventMap);
                });
    }

    private RoomState(GlobalStateHolder globalStateHolder, IRoom room, List<AbstractRoomStateEvent> stateEvents,
                      Map<String, MembershipEvent> latestMembershipEventMap) {
        this.globalStateHolder = globalStateHolder;
        this.room = room;
        this.stateEvents = stateEvents;
        this.latestMembershipEventMap = latestMembershipEventMap;
        ((Room) room).setGlobalStateHolder(globalStateHolder);
    }

}
