package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import im.joker.api.vo.sync.SyncResponse;
import im.joker.device.IDevice;
import im.joker.event.room.IRoomEvent;
import im.joker.helper.EventSyncQueueManager;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.LongPollingHelper;
import im.joker.helper.RoomSubscribeManager;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private GlobalStateHolder globalStateHolder;
    @Autowired
    private LongPollingHelper longPollingHelper;
    @Autowired
    private EventSyncQueueManager eventSyncQueueManager;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;

    @Override
    public Mono<Void> roomEventProcess(IRoomEvent event) {
        return Mono.empty();
    }

    @Override
    public Mono<SyncResponse> syncProcess(SyncRequest request, IDevice loginDevice) {
        Flux<String> deviceCareRoomIds = roomSubscribeManager.retrieveRooms(loginDevice.getDeviceId());
        boolean fullState = request.getFullState() != null && request.getFullState();
        if (fullState || StringUtils.isBlank(request.getSince())) {
            // todo 返回全部的状态信息
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
