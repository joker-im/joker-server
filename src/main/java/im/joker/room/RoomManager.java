package im.joker.room;

import com.google.common.collect.Lists;
import im.joker.api.vo.room.CreateRoomRequest;
import im.joker.device.IDevice;
import im.joker.event.ImEventBuilder;
import im.joker.event.MembershipType;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.MembershipEvent;
import im.joker.event.room.state.RoomCreateEvent;
import im.joker.event.room.state.RoomNameEvent;
import im.joker.event.room.state.RoomTopicEvent;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.IdGenerator;
import im.joker.store.ReactiveMongodbStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RoomManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Autowired
    private ImEventBuilder imEventBuilder;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private GlobalStateHolder globalStateHolder;


    public Mono<IRoom> createRoom(IDevice device, CreateRoomRequest createRoomRequest) {

        LocalDateTime now = LocalDateTime.now();
        Long nowTimestamp = now.toInstant(ZoneOffset.of("+8")).toEpochMilli();

        IRoom room = Room.builder().creator(device.getUserId())
                .createTime(now)
                .direct(createRoomRequest.getDirect() != null && createRoomRequest.getDirect())
                .visibility(createRoomRequest.getVisibility())
                .roomId(idGenerator.newRoomId())
                .globalStateHolder(globalStateHolder)
                .build();

        RoomCreateEvent roomCreateEvent = imEventBuilder.roomCreateEvent(device.getUserId(), room.getRoomId(), device.getUserId(), nowTimestamp);
        List<MembershipEvent> beInvitedUserEvents = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(createRoomRequest.getInvite())) {
            beInvitedUserEvents = createRoomRequest.getInvite()
                    .stream()
                    .map(e -> imEventBuilder.membershipEvent(room.getRoomId(), nowTimestamp,
                            device.getUserId(), e, "", "", MembershipType.invite)
                    ).collect(Collectors.toList());
        }
        RoomNameEvent roomNameEvent = null;
        if (StringUtils.isNotBlank(createRoomRequest.getName())) {
            roomNameEvent = imEventBuilder.roomNameEvent(createRoomRequest.getName(),
                    room.getRoomId(), device.getUserId(), nowTimestamp);
        }
        RoomTopicEvent roomTopicEvent = null;
        if (StringUtils.isNotBlank(createRoomRequest.getTopic())) {
            roomTopicEvent = imEventBuilder.roomTopicEvent(createRoomRequest.getTopic(), room.getRoomId(), device.getDeviceId(), nowTimestamp);
        }
        List<AbstractRoomStateEvent> totalEvents = Lists.newArrayList(roomCreateEvent, roomNameEvent, roomTopicEvent)
                .stream().filter(Objects::nonNull).collect(Collectors.toList());
        List<AbstractRoomStateEvent> initRoomStateEvent = createRoomRequest.getInitialState()
                .stream().peek(e -> imEventBuilder.setCommonEventFiled(e, room.getRoomId(), device.getUserId(), nowTimestamp)).collect(Collectors.toList());

        totalEvents.addAll(beInvitedUserEvents);
        totalEvents.addAll(initRoomStateEvent);
        return mongodbStore.addRoom(room)
                .flatMapMany(e -> Flux.fromIterable(totalEvents))
                .flatMap(event -> idGenerator.nextEventStreamId()
                        .flatMap(streamId -> {
                            event.setStreamId(streamId);
                            return room.inject(event);
                        }))
                .collectList()
                .flatMap(e -> Mono.just(room));

    }


}
