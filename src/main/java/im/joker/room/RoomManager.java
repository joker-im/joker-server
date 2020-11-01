package im.joker.room;

import com.google.common.collect.Lists;
import im.joker.api.vo.room.CreateRoomRequest;
import im.joker.device.IDevice;
import im.joker.event.*;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.*;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.IdGenerator;
import im.joker.store.IStore;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Component
@Slf4j
public class RoomManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Autowired
    private ImEventBuilder eventBuilder;
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private IStore mongodbStore;
    @Autowired
    private GlobalStateHolder globalStateHolder;


    /**
     * 初始化创房,会有很多的附带事件被inject
     *
     * @param device
     * @param createRoomRequest
     * @return
     */
    public Mono<IRoom> createRoom(IDevice device, CreateRoomRequest createRoomRequest) {

        LocalDateTime now = LocalDateTime.now();

        IRoom room = Room.builder().creator(device.getUserId())
                .createTime(now)
                .direct(createRoomRequest.getDirect() != null && createRoomRequest.getDirect())
                .visibility(createRoomRequest.getVisibility())
                .roomId(idGenerator.newRoomId())
                .globalStateHolder(globalStateHolder)
                .build();
        // 先创房
        RoomCreateEvent roomCreateEvent = eventBuilder.roomCreateEvent(device.getUserId(), room.getRoomId(), device.getUserId(), now);

        // 自己加入
        MembershipEvent membershipEvent = eventBuilder.membershipEvent(room.getRoomId(), now,
                device.getUserId(), device.getUserId(), device.getName(), "", MembershipType.Join);

        // 默认的权限设置事件
        PowerLevelEvent powerLevelEvent = eventBuilder.defaultPowerLevelEvent(room.getRoomId(), device.getUserId(), now);
        // 可能存在覆盖权限事件
        if (createRoomRequest.getPowerLevelContentOverride() != null) {
            powerLevelEvent.setContent(createRoomRequest.getPowerLevelContentOverride());
        }
        // 房间加入规则
        RoomJoinRuleEvent joinRuleEvent =
                eventBuilder.roomJoinRuleEvent(RoomJoinRuleType.Invite, room.getRoomId(), device.getUserId(), now);

        PresetType presetType = PresetType.find(createRoomRequest.getPreset());
        if (presetType == null) {
            if ("public".equals(createRoomRequest.getVisibility())) {
                joinRuleEvent = eventBuilder.roomJoinRuleEvent(RoomJoinRuleType.Public, room.getRoomId(), device.getUserId(), now);
            }
        } else {
            if (presetType.equals(PresetType.Trusted_private_chat)) {
                joinRuleEvent = eventBuilder.roomJoinRuleEvent(RoomJoinRuleType.Public, room.getRoomId(), device.getUserId(), now);
            }
        }

        // 可能存在邀请了某些用户
        List<MembershipEvent> beInvitedUserEvents = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(createRoomRequest.getInvite())) {
            beInvitedUserEvents = createRoomRequest.getInvite()
                    .stream()
                    .map(e -> eventBuilder.membershipEvent(room.getRoomId(), now,
                            device.getUserId(), e, "", "", MembershipType.Invite)
                    ).collect(Collectors.toList());
        }

        // 初始事件
        List<AbstractRoomStateEvent> initRoomStateEvent = createRoomRequest.getInitialState()
                .stream().peek(e -> eventBuilder.setCommonEventFiled(e, room.getRoomId(), device.getUserId(), now)).collect(Collectors.toList());


        // 其他事件
        RoomNameEvent roomNameEvent = null;
        if (StringUtils.isNotBlank(createRoomRequest.getName())) {
            roomNameEvent = eventBuilder.roomNameEvent(createRoomRequest.getName(),
                    room.getRoomId(), device.getUserId(), now);
        }
        RoomTopicEvent roomTopicEvent = null;
        if (StringUtils.isNotBlank(createRoomRequest.getTopic())) {
            roomTopicEvent = eventBuilder.roomTopicEvent(createRoomRequest.getTopic(), room.getRoomId(), device.getUserId(), now);
        }

        // 汇聚
        List<AbstractRoomStateEvent> totalEvents = Lists.newArrayList(roomCreateEvent,
                membershipEvent, powerLevelEvent, joinRuleEvent, roomNameEvent, roomTopicEvent)
                .stream().filter(Objects::nonNull).collect(Collectors.toList());

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


    public Flux<ImEvent> findEvents(EventType eventType, String userId) {
        return mongodbStore.findEvents(eventType, userId);
    }

    public Mono<ImEvent> inviteToRoom(String targetRoomId, String targetUserId, String sender) {
        return RoomState.existRoomState(targetRoomId, globalStateHolder)
                .zipWhen(roomState -> checkInvitePower(roomState.getUserStateEvents().get(sender)))
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 ->
                        idGenerator.nextEventStreamId()
                                .flatMap(streamId -> {
                                    Room room = (Room) tuple2.getT1().getRoom();
                                    room.setGlobalStateHolder(globalStateHolder);
                                    MembershipEvent mEvent = eventBuilder
                                            .membershipEvent(targetRoomId, LocalDateTime.now(), sender, targetUserId,
                                                    "", "", MembershipType.Invite);
                                    mEvent.setStreamId(streamId);
                                    return room.inject(mEvent);
                                })
                );
    }

    /**
     * 是否有邀请的权限
     *
     * @param membershipEvents
     * @return
     */
    private Mono<Boolean> checkInvitePower(List<AbstractRoomStateEvent> membershipEvents) {
        if (CollectionUtils.isEmpty(membershipEvents)) {
            return Mono.empty();
        }
        boolean in = false;
        for (ImEvent membershipEvent : membershipEvents) {
            if (!(membershipEvent instanceof MembershipEvent)) {
                continue;
            }
            MembershipContent content = (MembershipContent) membershipEvent.getContent();
            if (MembershipType.Join.is(content.getMembership())) {
                in = true;
            } else if (MembershipType.Leave.is(content.getMembership())) {
                in = false;
            }
        }
        if (in) {
            return Mono.just(true);
        } else {
            return Mono.empty();
        }

    }


    public Mono<IRoom> findRoom(String roomId) {
        return mongodbStore.findRoomByRoomId(roomId);
    }


    @SneakyThrows
    public static void main(String[] args) {
        Flux<String> flux = Flux.just("1", "2", "3");
        Mono<String> mono = Mono.empty();
        Flux.zip(flux, mono)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST))).subscribe();
        TimeUnit.SECONDS.sleep(10);
    }
}
