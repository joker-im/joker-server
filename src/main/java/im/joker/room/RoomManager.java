package im.joker.room;

import com.google.common.collect.Lists;
import im.joker.api.vo.room.CreateRoomRequest;
import im.joker.device.IDevice;
import im.joker.event.*;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.state.*;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.EventAuthorizationValidator;
import im.joker.helper.GlobalStateHolder;
import im.joker.helper.IdGenerator;
import im.joker.store.IStore;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
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
    @Autowired
    private EventAuthorizationValidator eventAuthorizationValidator;


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
        // todo 还差个visibility 事件

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
        return findRoomState(targetRoomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostInviteEvent(roomState, sender)))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 ->
                        idGenerator.nextEventStreamId()
                                .flatMap(streamId -> {
                                    Room room = (Room) tuple2.getT1().getRoom();
                                    MembershipEvent mEvent = eventBuilder
                                            .membershipEvent(targetRoomId, LocalDateTime.now(), sender, targetUserId,
                                                    "", "", MembershipType.Invite);
                                    mEvent.setStreamId(streamId);
                                    return room.inject(mEvent);
                                })
                );
    }


    public Mono<RoomState> findRoomState(String roomId) {
        return RoomState.existRoomState(roomId, globalStateHolder);
    }


    public Mono<ImEvent> joinRoom(String sender, String targetRoomId) {
        return findRoomState(targetRoomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostJoinEvent(roomState, sender)))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 -> idGenerator.nextEventStreamId().flatMap(streamId -> {
                    Room room = (Room) tuple2.getT1().getRoom();
                    MembershipEvent join = eventBuilder
                            .membershipEvent(targetRoomId, LocalDateTime.now(), sender, sender, "", "", MembershipType.Join);
                    return room.inject(join);
                }));

    }

    /**
     * 有2层意思.
     * 1. 当前用户不接受邀请, 也会调用level,表示拒绝加入该房间
     * 2. 当前用户已经在此房间,但是自己想走
     *
     * @param sender
     * @param roomId
     * @return
     */
    public Mono<ImEvent> levelRoom(String sender, String roomId) {

        return findRoomState(roomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostLeaveEvent(roomState, sender)))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 -> idGenerator.nextEventStreamId().flatMap(streamId -> {
                    Room room = (Room) tuple2.getT1().getRoom();
                    MembershipEvent leave = eventBuilder.
                            membershipEvent(roomId, LocalDateTime.now(), sender, sender, "", "", MembershipType.Leave);
                    return room.inject(leave);
                }));
    }
}
