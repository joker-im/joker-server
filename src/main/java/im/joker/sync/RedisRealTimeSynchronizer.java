package im.joker.sync;

import com.google.common.collect.Lists;
import im.joker.api.vo.sync.SyncRequest;
import im.joker.api.vo.sync.SyncResponse;
import im.joker.device.IDevice;
import im.joker.event.MembershipType;
import im.joker.event.content.IContent;
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
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * 以房间为单位，缓存等待同步的最新消息到redis集合中（实时维护）
 *
 * @Author LinYuRong
 * @Date 2020/10/13 17:22
 * @Version 1.0
 */
@Component
@Slf4j
public class RedisRealTimeSynchronizer implements IRealTimeSynchronizer {

    @Autowired
    private LongPollingHelper longPollingHelper;
    @Autowired
    private EventSyncQueueManager eventSyncQueueManager;
    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;

    private final int limitOfRoom = 30;

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

        SyncResponse.Rooms rooms = SyncResponse.Rooms.builder().build();

        Map<String, SyncResponse.InvitedRooms> invitedMap = new ConcurrentHashMap<>(16);
        Map<String, SyncResponse.LeftRooms> leftMap = new ConcurrentHashMap<>(16);
        Map<String, SyncResponse.JoinedRooms> joinedRoomMap = new ConcurrentHashMap<>(16);
        LongAdder withoutNewMessageCount = new LongAdder();


        rooms.setJoin(joinedRoomMap);
        rooms.setInvite(invitedMap);
        rooms.setLeave(leftMap);

        AtomicReference<Long> maxStreamId = new AtomicReference<>();

        // 当sinceId不为空时候, 从device监听的房间里面每个房间拿n条消息
        eventSyncQueueManager.takeRelatedEvents(loginDevice.getDeviceId(), limitOfRoom)
                .zipWith(mongodbStore.findLatestStreamId())
                .map(tuple2 -> {
                    maxStreamId.set(tuple2.getT2());
                    return tuple2.getT1();
                })
                // 查询这些房间所有的状态信息
                .zipWhen(roomEventMap -> mongodbStore.findRoomStateEvents(roomEventMap.keySet(), sinceId)
                        .collect(Collectors.groupingBy(AbstractRoomEvent::getRoomId)))
                .flatMap(tuple2 -> {
                    Map<String, List<AbstractRoomEvent>> roomEventMap = tuple2.getT1();
                    Map<String, List<AbstractRoomStateEvent>> fullRoomStateMap = tuple2.getT2();

                    withoutNewMessageCount.add(roomEventMap.size());

                    roomEventMap.forEach((roomId, roomEvents) -> {

                        SyncResponse.JoinedRooms joinedRoom = SyncResponse.JoinedRooms.builder().build();
                        SyncResponse.LeftRooms leftRoom = SyncResponse.LeftRooms.builder().build();

                        List<AbstractRoomStateEvent> beforeSinceStateEvents = fullRoomStateMap.get(roomId);

                        if (CollectionUtils.isEmpty(beforeSinceStateEvents)) {
                            log.warn("roomId:{} 不存在任何状态信息", roomId);
                            return;
                        }
                        RoomState beforeSinceRoomState = RoomState.from(beforeSinceStateEvents);
                        MembershipEvent beforeSinceMembershipEvent = beforeSinceRoomState.searchMembershipEvent(loginDevice.getUserId());

                        List<AbstractRoomEvent> afterSinceEvents = roomEvents.stream().filter(e -> e.getStreamId() >= sinceId).collect(Collectors.toList());


                        // 当copy的消息队列 > n 条的时候, 需要从sinceId之后查询该房间的状态,并设置limit = true
                        if (afterSinceEvents.size() > limitOfRoom) {
                            int afterSinceEventSize = afterSinceEvents.size();
                            List<AbstractRoomEvent> timeLineEvents = afterSinceEvents.stream()
                                    .skip(afterSinceEventSize - limitOfRoom)
                                    .filter(e -> e.getStreamId() > beforeSinceRoomState.getEarliestRoomStateEvent().getStreamId())
                                    .collect(Collectors.toList());
                            SyncResponse.Timeline timeline = SyncResponse.Timeline.builder()
                                    .events(timeLineEvents)
                                    .limited(true)
                                    .prevBatch(String.valueOf(0))
                                    .build();
                            SyncResponse.State state = SyncResponse.State.builder()
                                    .events(beforeSinceRoomState.distinctStateEvents())
                                    .build();

                            if (beforeSinceMembershipEvent != null) {
                                MembershipContent content = (MembershipContent) beforeSinceMembershipEvent.getContent();
                                if (MembershipType.Leave.is(content.getMembership())) {
                                    leftRoom.setTimeline(timeline);
                                    leftRoom.setState(state);
                                    leftMap.put(roomId, leftRoom);
                                }
                                if (MembershipType.Join.is(content.getMembership())) {
                                    joinedRoom.setTimeline(timeline);
                                    joinedRoomMap.put(roomId, joinedRoom);
                                }

                            }

                            return;
                        }
                        // 当一条消息都没有时候
                        if (afterSinceEvents.isEmpty()) {
                            withoutNewMessageCount.add(-1);
                            return;
                        }

                        // 当 copy的消息队列 <= n条的时候,全部放到timeline
                        SyncResponse.Timeline timeline = SyncResponse.Timeline.builder()
                                .events(afterSinceEvents)
                                .limited(false)
                                .prevBatch(String.valueOf(afterSinceEvents.get(0).getStreamId()))
                                .build();

                        if (beforeSinceMembershipEvent != null) {
                            MembershipContent content = (MembershipContent) beforeSinceMembershipEvent.getContent();
                            if (MembershipType.Leave.is(content.getMembership())) {
                                leftRoom.setTimeline(timeline);
                                leftMap.put(roomId, leftRoom);
                            }
                            if (MembershipType.Join.is(content.getMembership())) {
                                joinedRoom.setTimeline(timeline);
                                joinedRoomMap.put(roomId, joinedRoom);
                            }

                        }

                    });

                    if (withoutNewMessageCount.intValue() == 0) {
                        log.warn("长轮询开始~");
                        return Mono.create((MonoSink<Boolean> monoSink) -> {
                                    longPollingHelper.addSyncDevice(loginDevice.getDeviceId(), monoSink);
                                }
                        );
                    }

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
        AtomicReference<Long> maxStreamId = new AtomicReference<>();
        AtomicReference<List<String>> roomIds = new AtomicReference<>();
        // todo 返回全部的状态信息
        return roomSubscribeManager.retrieveRooms(loginDevice.getDeviceId())
                .collectList()
                .zipWith(mongodbStore.findLatestStreamId())
                // 这些房间都返回最新的30条数据,并且返回所有的状态事件
                .flatMapMany(tuple2 -> {
                    maxStreamId.set(tuple2.getT2());
                    roomIds.set(tuple2.getT1());
                    return mongodbStore.findRoomStateEvents(tuple2.getT1(), tuple2.getT2());
                })
                .collect(Collectors.groupingBy(AbstractRoomEvent::getRoomId))
                .zipWith(mongodbStore.findEventGroupByRoomTopK(roomIds.get(), limitOfRoom, false))
                .map(tuple2 -> {
                    // 每个房间的状态事件,用于组装state
                    Map<String, List<AbstractRoomStateEvent>> fullRoomStateMap = tuple2.getT1();
                    // 每个房间最新的topK事件
                    Map<String, List<AbstractRoomEvent>> latestRoomEventMap = tuple2.getT2();

                    latestRoomEventMap.forEach((roomId, latestRoomEvents) -> {

                        SyncResponse.InvitedRooms invitedRoom = SyncResponse.InvitedRooms.builder().build();
                        SyncResponse.JoinedRooms joinedRoom = SyncResponse.JoinedRooms.builder().build();
                        SyncResponse.LeftRooms leftRoom = SyncResponse.LeftRooms.builder().build();

                        // 取出topK最早的那条事件,在此条事件之前封装成state
                        AbstractRoomEvent earliestOfLatestRoomEvent = latestRoomEvents.get(latestRoomEvents.size() - 1);
                        if (fullRoomStateMap.containsKey(roomId)) {
                            // 算出此事件前的状态
                            List<AbstractRoomStateEvent> beforeStateEvents = fullRoomStateMap.get(roomId)
                                    .stream()
                                    .filter(e -> e.getStreamId() < earliestOfLatestRoomEvent.getStreamId())
                                    .collect(Collectors.toList());

                            RoomState beforeRoomState = RoomState.from(beforeStateEvents);
                            MembershipEvent membershipEvent = beforeRoomState.searchMembershipEvent(loginDevice.getUserId());
                            if (membershipEvent != null) {

                                MembershipContent membershipContent = (MembershipContent) membershipEvent.getContent();
                                SyncResponse.State state = SyncResponse.State.builder().events(beforeRoomState.distinctStateEvents()).build();
                                SyncResponse.Timeline timeline = SyncResponse.Timeline.builder()
                                        .events(latestRoomEvents)
                                        .limited(true)
                                        .prevBatch(String.valueOf(0))
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
                    return SyncResponse.builder().rooms(rooms).nextBatch(String.valueOf(maxStreamId.get() + 1)).build();
                });
    }


    @Override
    public Flux<String> getActiveRoomsOfDevice(String deviceId) {
        return Flux.empty();
    }
}
