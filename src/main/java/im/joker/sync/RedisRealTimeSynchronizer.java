package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import im.joker.api.vo.sync.SyncResponse;
import im.joker.device.IDevice;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.IRoomEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.helper.EventSyncQueueManager;
import im.joker.helper.LongPollingHelper;
import im.joker.helper.RoomSubscribeManager;
import im.joker.room.RoomState;
import im.joker.store.ReactiveMongodbStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

/**
 * 以房间为单位，缓存等待同步的最新消息到redis集合中（实时维护）
 *
 * @Author LinYuRong
 * @Date 2020/10/13 17:22
 * @Version 1.0
 */
@Component
public class RedisRealTimeSynchronizer implements IRealTimeSynchronizer {

    @Autowired
    private LongPollingHelper longPollingHelper;
    @Autowired
    private EventSyncQueueManager eventSyncQueueManager;
    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;

    @Override
    public Mono<Void> roomEventProcess(IRoomEvent event) {
        return Mono.empty();
    }

    @Override
    public Mono<SyncResponse> syncProcess(SyncRequest request, IDevice loginDevice) {
        boolean fullState = request.getFullState() != null && request.getFullState();
        if (fullState || StringUtils.isBlank(request.getSince())) {
            // todo 返回全部的状态信息
            roomSubscribeManager.retrieveRooms(loginDevice.getDeviceId())
                    .collectList()
                    .flatMap(roomIds -> mongodbStore.findRoomStateEvents(roomIds)
                            .collect(Collectors.groupingBy(AbstractRoomEvent::getRoomId)))
                    .flatMap(roomEventsMap -> {
                        SyncResponse.Rooms rooms = SyncResponse.Rooms.builder().build();
                        roomEventsMap.forEach((roomId, events) -> {
                            // 从状态事件list获取出当前的roomState

                            RoomState roomState = RoomState.from(events);
                            MembershipEvent membershipEvent = roomState.searchMembershipEvent(loginDevice.getDeviceId());
                            if (membershipEvent == null) {
                                return;
                            }
                        });
                        return Mono.empty();
                    });

        }


        //        SyncRoomEventAdder adder = new SyncRoomEventAdder();
//        eventQueueManager.takeRelatedEvents(loginDevice.getDeviceId(), 30);
//        return getActiveRoomsOfDevice(loginDevice.getDeviceId())
//                .flatMap(roomId -> globalStateHolder.getRedisTemplate().opsForList()
//                        // TODO limit
//                        .range(String.format(ImRedisKeys.ACTIVE_ROOM_LATEST_EVENTS, roomId), -30, -1)
//                        .map(o -> {
//                            try {
//                                return (IRoomEvent) redisEventSerializer.deserialize(o);
//                            } catch (Exception e) {
//                                throw new MalformedEventException("sync事件反序列出错");
//                            }
//                        })
//                        .sort(Comparator.comparingLong(IRoomEvent::getStreamId).reversed())
//                        .collectList()
//                        .map(events -> new SyncEvents(roomId, events)))
//                .collectList()
//                .flatMap(o -> {
//                    adder.addEventList(o);
//                    if (adder.isExistsData()) {
//                        return Mono.just(adder.get());
//                    } else {
//                        return Mono.create((Consumer<MonoSink<Boolean>>) monoSink -> {
//                            longPollingHelper.addSyncDevice(loginDevice.getDeviceId(), monoSink);
//                        });
//                    }
//                })
//                .map(e -> SyncResponse.builder().build())
//                .timeout(Duration.ofMillis(request.getTimeout().longValue()), Mono.just(SyncResponse.builder().build()))
//                .map(e -> SyncResponse.builder().build());
        return Mono.empty();
    }

    @Override
    public Flux<String> getActiveRoomsOfDevice(String deviceId) {
        return Flux.empty();
    }
}
