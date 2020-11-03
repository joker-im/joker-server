package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import im.joker.constants.ImRedisKeys;
import im.joker.device.IDevice;
import im.joker.event.room.IRoomEvent;
import im.joker.exception.MalformedEventException;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.RedisEventSerializer;
import im.joker.sync.entity.SyncEvents;
import im.joker.sync.entity.SyncResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 以房间为单位，缓存等待同步的最新消息到redis集合中（实时维护）
 *
 * @Author LinYuRong
 * @Date 2020/10/13 17:22
 * @Version 1.0
 */
@Component
public class RedisIRealTimeSynchronizer implements IRealTimeSynchronizer {

    @Autowired
    private GlobalStateHolder globalStateHolder;
    @Autowired
    private RedisEventSerializer redisEventSerializer;

    /**
     * sync请求等待响应列表
     */
    private Map<String, MonoSink<SyncResponse>> waitingSyncMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> roomEventProcess(IRoomEvent event) {
        return Mono.empty();
    }

    @Override
    public Mono<SyncResponse> syncProcess(SyncRequest request, IDevice device) {
        SyncRoomEventAdder adder = new SyncRoomEventAdder();
        return getActiveRoomsOfDevice(device.getDeviceId())
                .flatMap(roomId -> globalStateHolder.getRedisTemplate().opsForList()
                        // TODO limit
                        .range(String.format(ImRedisKeys.ACTIVE_ROOM_LATEST_EVENTS, roomId), -30, -1)
                        .map(o -> {
                            try {
                                return (IRoomEvent) redisEventSerializer.deserialize(o);
                            } catch (Exception e) {
                                throw new MalformedEventException("sync事件反序列出错");
                            }
                        })
                        .sort(Comparator.comparingLong(IRoomEvent::getStreamId).reversed())
                        .collectList()
                        .map(events -> new SyncEvents(roomId, events)))
                .collectList()
                .flatMap(o -> {
                    adder.addEventList(o);
                    if (adder.isExistsData()) {
                        return Mono.just(adder.get());
                    } else {
                        return Mono.create(monoSink -> {
                            waitingSyncMap.put(device.getDeviceId(), monoSink);
                        });
                    }
                });
    }

    @Override
    public Flux<String> getActiveRoomsOfDevice(String deviceId) {
        return Flux.empty();
    }
}
