package im.joker.room;

import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
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

    private final List<ImEvent> stateEvents;

    private final Map<String, List<AbstractRoomStateEvent>> userStateEvents;

    private final IRoom room;


    public static Mono<RoomState> existRoomState(String roomId, GlobalStateHolder globalStateHolder) {
        Flux<ImEvent> eventFlux = globalStateHolder.getMongodbStore().findRoomStateEvents(roomId);
        Mono<IRoom> roomMono = globalStateHolder.getMongodbStore().findRoomByRoomId(roomId);
        return eventFlux
                // 逆序
                .collectSortedList((a, b) -> ((AbstractRoomStateEvent) b).getStreamId().compareTo(((AbstractRoomStateEvent) a).getStreamId()))
                .zipWith(roomMono)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "房间不存在")))
                .map(tuple2 -> {
                    Map<String, List<AbstractRoomStateEvent>> userStateEventMap = tuple2.getT1().stream()
                            .map(e -> (AbstractRoomStateEvent) e)
                            .collect(Collectors.groupingBy(AbstractRoomEvent::getSender, Collectors.toList()));
                    return new RoomState(globalStateHolder, tuple2.getT2(), tuple2.getT1(), userStateEventMap);
                });
    }

    private RoomState(GlobalStateHolder globalStateHolder, IRoom room, List<ImEvent> stateEvents,
                      Map<String, List<AbstractRoomStateEvent>> userStateEvents) {
        this.globalStateHolder = globalStateHolder;
        this.room = room;
        this.stateEvents = stateEvents;
        this.userStateEvents = userStateEvents;
        ((Room) room).setGlobalStateHolder(globalStateHolder);
    }

}