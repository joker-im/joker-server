package im.joker.helper;

import com.google.common.collect.Maps;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.util.GsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static im.joker.constants.ImRedisKeys.ACTIVE_ROOM_LATEST_EVENTS;

@Component
public class EventSyncQueueManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private final int limit = 100;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;
    @Autowired
    private RequestProcessor requestProcessor;

    /**
     * 从对应的设备中到自己关心的队列里,拿出对应限制条数的消息.是有序的
     * key是房间, value是此房间的事件集
     *
     * @param deviceId
     * @param limitOfRoom
     * @return
     */
    public Mono<Map<String, List<AbstractRoomEvent>>> takeRelatedEvents(String deviceId, int limitOfRoom) {
        Flux<String> careRoomIds = roomSubscribeManager.retrieveRooms(deviceId);
        return careRoomIds.flatMap(roomId -> {
            return Mono.just(roomId)
                    .zipWith(redisTemplate.opsForList().range(String.format(ACTIVE_ROOM_LATEST_EVENTS, roomId), 0, limitOfRoom)
                            .map(s -> requestProcessor.toBean(s, AbstractRoomEvent.class))
                            .collectList());
        }).collectSortedList()
                .map(tuple2s -> {
                    Map<String, List<AbstractRoomEvent>> retMap = Maps.newHashMap();
                    tuple2s.forEach(tuple2 -> retMap.put(tuple2.getT1(), tuple2.getT2()));
                    return retMap;
                });
    }


    /**
     * 将消息存到sync时的消息队列,并且每个房间只维持最新的100条数据
     *
     * @param roomEvent
     * @return
     */
    public Mono<Void> addEventToQueue(AbstractRoomEvent roomEvent) {

        return redisTemplate.opsForList().rightPush(String.format(ACTIVE_ROOM_LATEST_EVENTS, roomEvent.getRoomId()), GsonUtils.get().toJson(roomEvent))
                .flatMap(e -> redisTemplate.opsForList().size(String.format(ACTIVE_ROOM_LATEST_EVENTS, roomEvent.getRoomId())))
                .flatMap(size -> {
                    if (size > limit) {
                        return redisTemplate.opsForList().trim(String.format(ACTIVE_ROOM_LATEST_EVENTS, roomEvent.getRoomId()), size - limit, -1);
                    }
                    return Mono.empty();
                })
                .then();

    }


}
