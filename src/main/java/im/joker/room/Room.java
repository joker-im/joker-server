package im.joker.room;

import im.joker.device.IDevice;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.helper.GlobalStateHolder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@Document
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Room implements IRoom {
    @Id
    private String id;

    private String roomId;

    private String creator;

    private LocalDateTime createTime;

    private transient GlobalStateHolder globalStateHolder;

    private Boolean direct;

    private String visibility;

    /**
     * 需要做几件事情
     * 1. 将消息落盘
     * 2. 维护房间与设备的订阅关系
     * 3. 将消息发往redis的房间队列
     * 4. 唤醒正在sync的设备
     *
     * @param ev
     * @param device
     * @return
     */
    @Override
    public Mono<AbstractRoomEvent> injectEvent(AbstractRoomEvent ev, IDevice device) {
        return globalStateHolder.getMongodbStore().addEvent(ev)
                .flatMap(e -> {
                    Mono<Void> updateSubscribeOps = globalStateHolder.getRoomSubscribeManager().updateRelation(device.getDeviceId(), e);
                    Mono<Void> sendMessageQueueOps = globalStateHolder.getEventSyncQueueManager().addEventToQueue(ev);
                    return Mono.just(e)
                            .takeUntilOther(
                                    Mono.zip(updateSubscribeOps, sendMessageQueueOps)
                                            .then(globalStateHolder.getLongPollingHelper().notifySyncDevice(device.getDeviceId()))
                            );
                });

    }

    @Override
    public Flux<AbstractRoomEvent> injectEvents(List<AbstractRoomEvent> evs, IDevice device) {
        return Flux.fromIterable(evs)
                .flatMap(ev -> {
                    Mono<Void> updateSubscribeOps = globalStateHolder.getRoomSubscribeManager().updateRelation(device.getDeviceId(), ev);
                    Mono<Void> sendMessageQueueOps = globalStateHolder.getEventSyncQueueManager().addEventToQueue(ev);
                    return Mono.just(ev)
                            .takeUntilOther(Mono.zip(updateSubscribeOps, sendMessageQueueOps));
                })
                .doOnComplete(() -> globalStateHolder.getLongPollingHelper().notifySyncDevice(device.getDeviceId()).subscribe());


    }

    @Override
    public RoomState getCurrentState() {
        return null;
    }

    @Override
    public RoomState getStateFor(String eventId) {
        return null;
    }
}
