package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import im.joker.api.vo.sync.SyncResponse;
import im.joker.device.IDevice;
import im.joker.event.MembershipType;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
            return initSync(loginDevice);
        }

        long sinceId = Long.parseLong(request.getSince());
        // 当sinceId不为空时候, 从device监听的房间里面每个房间拿30条消息
        eventSyncQueueManager.takeRelatedEvents(loginDevice.getDeviceId(), 30)
                .map(roomEventMap -> {
                    SyncResponse.Rooms rooms = SyncResponse.Rooms.builder().build();
                    Map<String, SyncResponse.JoinedRooms> joinedRoomMap = new ConcurrentHashMap<>(16);
                    Map<String, SyncResponse.InvitedRooms> invitedMap = new ConcurrentHashMap<>(16);
                    Map<String, SyncResponse.LeftRooms> leftMap = new ConcurrentHashMap<>(16);
                    rooms.setInvite(invitedMap);
                    rooms.setJoin(joinedRoomMap);
                    rooms.setLeave(leftMap);
                    roomEventMap.forEach((roomId, roomEvents) -> {

                    });
                });


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

    private Mono<SyncResponse> initSync(IDevice loginDevice) {
        SyncResponse.Rooms rooms = SyncResponse.Rooms.builder().build();
        Map<String, SyncResponse.JoinedRooms> joinedRoomMap = new ConcurrentHashMap<>(16);
        Map<String, SyncResponse.InvitedRooms> invitedMap = new ConcurrentHashMap<>(16);
        Map<String, SyncResponse.LeftRooms> leftMap = new ConcurrentHashMap<>(16);
        rooms.setInvite(invitedMap);
        rooms.setJoin(joinedRoomMap);
        rooms.setLeave(leftMap);
        // todo 返回全部的状态信息
        return roomSubscribeManager.retrieveRooms(loginDevice.getDeviceId())
                .collectList()
                // 这些房间都返回最新的30条数据,并且返回所有的状态事件
                .flatMap(roomIds -> mongodbStore.findRoomStateEvents(roomIds)
                        .collect(Collectors.groupingBy(AbstractRoomEvent::getRoomId))
                        .zipWith(mongodbStore.findEventGroupByRoomTopK(roomIds, 30))
                )
                .map(tuple2 -> {
                    final long[] maxStreamId = {-1};
                    // 每个房间的状态事件,用于组装state
                    Map<String, List<AbstractRoomStateEvent>> fullRoomStateMap = tuple2.getT1();
                    // 每个房间最新的topK事件
                    Map<String, List<AbstractRoomEvent>> latestRoomEventMap = tuple2.getT2();

                    latestRoomEventMap.forEach((roomId, latestRoomEvents) -> {

                        SyncResponse.InvitedRooms invitedRoom = SyncResponse.InvitedRooms.builder().build();
                        SyncResponse.JoinedRooms joinedRoom = SyncResponse.JoinedRooms.builder().build();
                        SyncResponse.LeftRooms leftRoom = SyncResponse.LeftRooms.builder().build();

                        // 取出topK最早的那条事件,在此条事件之前封装成state
                        AbstractRoomEvent earliestRoomEvent = latestRoomEvents.get(latestRoomEvents.size() - 1);
                        maxStreamId[0] = Math.max(maxStreamId[0], latestRoomEvents.get(0).getStreamId());
                        if (fullRoomStateMap.containsKey(roomId)) {
                            // 算出此事件前的状态
                            List<AbstractRoomStateEvent> beforeStateEvents = fullRoomStateMap.get(roomId)
                                    .stream()
                                    .filter(e -> e.getStreamId() < earliestRoomEvent.getStreamId())
                                    .collect(Collectors.toList());

                            MembershipEvent membershipEvent = RoomState.from(beforeStateEvents).searchMembershipEvent(loginDevice.getUserId());
                            if (membershipEvent != null) {
                                MembershipContent membershipContent = (MembershipContent) membershipEvent.getContent();
                                SyncResponse.State state = SyncResponse.State.builder().events(beforeStateEvents).build();
                                SyncResponse.Timeline timeline = SyncResponse.Timeline.builder()
                                        .events(latestRoomEvents)
                                        .limited(true)
                                        .prevBatch(String.valueOf(latestRoomEvents.get(0).getStreamId()))
                                        .build();

                                if (MembershipType.Invite.is(membershipContent.getMembership())) {
                                    invitedRoom.setInviteState(state);
                                    invitedMap.put(roomId, invitedRoom);
                                }
                                if (MembershipType.Leave.is(membershipContent.getMembership())) {
                                    leftRoom.setState(state);
                                    leftRoom.setTimeline(timeline);
                                    // todo accountData是什么鬼
                                    leftMap.put(roomId, leftRoom);
                                }
                                if (MembershipType.Join.is(membershipContent.getMembership())) {
                                    // todo 未读通知
                                    joinedRoom.setState(state);
                                    joinedRoom.setTimeline(timeline);
                                    joinedRoomMap.put(roomId, joinedRoom);
                                }
                            }
                        }
                    });
                    return SyncResponse.builder().rooms(rooms).nextBatch(String.valueOf(maxStreamId[0])).build();
                });
    }


    @Override
    public Flux<String> getActiveRoomsOfDevice(String deviceId) {
        return Flux.empty();
    }
}
