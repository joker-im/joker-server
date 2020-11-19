package im.joker.room;

import com.google.common.collect.Lists;
import im.joker.api.vo.room.CreateRoomRequest;
import im.joker.device.IDevice;
import im.joker.event.*;
import im.joker.event.content.state.MembershipContent;
import im.joker.event.room.AbstractRoomEvent;
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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
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
                device.getUserId(), null, device.getUserId(), device.getName(), "", MembershipType.Join);

        // 默认的权限设置事件
        PowerLevelEvent powerLevelEvent = eventBuilder.defaultPowerLevelEvent(room.getRoomId(), device.getUserId(), now);
        // 可能存在覆盖权限事件
        if (createRoomRequest.getPowerLevelContentOverride() != null) {
            powerLevelEvent.setContent(createRoomRequest.getPowerLevelContentOverride());
        }
        // 房间聊天记录规则
        HistoryVisibilityEvent historyVisibilityEvent = eventBuilder.defaultHistoryVisibilityEvent(room.getRoomId(), device.getUserId(), now);
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
                            device.getUserId(), null, e, "", "", MembershipType.Invite)
                    ).collect(Collectors.toList());
        }

        // 初始事件
        List<AbstractRoomStateEvent> initRoomStateEvent = createRoomRequest.getInitialState()
                .stream()
                .peek(e -> eventBuilder.setCommonEventFiled(e, room.getRoomId(), device.getUserId(), now))
                .collect(Collectors.toList());


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
                membershipEvent, powerLevelEvent, historyVisibilityEvent, joinRuleEvent, roomNameEvent, roomTopicEvent)
                .stream().filter(Objects::nonNull).collect(Collectors.toList());

        totalEvents.addAll(beInvitedUserEvents);
        totalEvents.addAll(initRoomStateEvent);
        return mongodbStore.addRoom(room)
                .flatMapMany(e -> Flux.fromIterable(totalEvents))
                .flatMap(event -> idGenerator.nextEventStreamId()
                        .map(streamId -> {
                            event.setStreamId(streamId);
                            return event;
                        }))
                .collectList()
                .flatMapMany(events -> {
                    List<AbstractRoomEvent> collect = events.stream().map(e -> (AbstractRoomEvent) e).collect(Collectors.toList());
                    return room.injectEvents(collect, device);
                })
                .then(Mono.just(room));

    }


    public Flux<AbstractRoomEvent> findMembershipEvents(EventType eventType, String stateKey) {
        return mongodbStore.findMembershipEvents(eventType, stateKey);
    }

    public Mono<ImEvent> inviteToRoom(String targetRoomId, String targetUserId, IDevice senderDevice) {
        return findRoomState(targetRoomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostInviteEvent(roomState, senderDevice.getUserId())))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 ->
                        idGenerator.nextEventStreamId()
                                .flatMap(streamId -> {
                                    IRoom room = tuple2.getT1().getRoom();
                                    MembershipEvent mEvent = eventBuilder
                                            .membershipEvent(targetRoomId, LocalDateTime.now(), senderDevice.getUserId(), null, targetUserId,
                                                    "", "", MembershipType.Invite);
                                    mEvent.setStreamId(streamId);
                                    return room.injectEvent(mEvent, senderDevice);
                                })
                );
    }


    public Mono<RoomState> findRoomState(String roomId) {
        return RoomState.getRoomState(roomId, globalStateHolder);
    }


    public Mono<ImEvent> joinRoom(IDevice senderDevice, String targetRoomId) {
        return findRoomState(targetRoomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostJoinEvent(roomState, senderDevice.getUserId())))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 -> idGenerator.nextEventStreamId().flatMap(streamId -> {
                    IRoom room = tuple2.getT1().getRoom();
                    MembershipEvent join = eventBuilder
                            .membershipEvent(targetRoomId, LocalDateTime.now(), senderDevice.getUserId(), null, senderDevice.getUserId(), "", "", MembershipType.Join);
                    join.setStreamId(streamId);
                    return room.injectEvent(join, senderDevice);
                }));

    }

    /**
     * 有2层意思.
     * 1. 当前用户不接受邀请, 也会调用level,表示拒绝加入该房间
     * 2. 当前用户已经在此房间,但是自己想走
     *
     * @param senderDevice
     * @param roomId
     * @return
     */
    public Mono<ImEvent> levelRoom(IDevice senderDevice, String roomId) {

        return findRoomState(roomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostLeaveEvent(roomState, senderDevice.getUserId())))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 -> idGenerator.nextEventStreamId().flatMap(streamId -> {
                    IRoom room = tuple2.getT1().getRoom();
                    MembershipEvent leave = eventBuilder.
                            membershipEvent(roomId, LocalDateTime.now(), senderDevice.getUserId(), null, senderDevice.getUserId(), "", "", MembershipType.Leave);
                    leave.setStreamId(streamId);
                    return room.injectEvent(leave, senderDevice);
                }));
    }

    public Mono<Void> kickMember(IDevice senderDevice, String targetUserId, String reason, String roomId) {

        return findRoomState(roomId)
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canKickMember(roomState, senderDevice.getUserId(), targetUserId)))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.FORBIDDEN)))
                .flatMap(tuple2 -> idGenerator.nextEventStreamId().flatMap(streamId -> {
                    IRoom room = tuple2.getT1().getRoom();
                    MembershipEvent leave = eventBuilder.membershipEvent(roomId, LocalDateTime.now(),
                            senderDevice.getUserId(), reason, targetUserId, "", "", MembershipType.Leave);
                    leave.setStreamId(streamId);
                    return room.injectEvent(leave, senderDevice);
                }))
                .then();
    }

    public Mono<String> sendMessageEvent(IDevice senderDevice, AbstractRoomEvent messageEvent) {

        return findRoomState(messageEvent.getRoomId())
                .zipWhen(roomState -> Mono.just(eventAuthorizationValidator.canPostMessageEvent(roomState, messageEvent)))
                .filter(Tuple2::getT2)
                .switchIfEmpty(Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "没有权限发送信息")))
                .flatMap(tuple2 -> tuple2.getT1().getRoom().injectEvent(messageEvent, senderDevice))
                .map(e -> messageEvent.getEventId());
    }


    /**
     * 此方法提供该userId所在的所有房间中,会回调传入每个房间最新的membershipContent以供选择关心的房间
     *
     * @param stateKey
     * @param predicate
     * @return
     */
    public Mono<List<String>> membershipAboutRooms(String stateKey, Predicate<MembershipContent> predicate) {
        // 查询该用户所有房间membership事件
        return findMembershipEvents(EventType.Membership, stateKey).collectList()
                .map(events -> {
                    // 取出每个房间里面最新的那条membership,组成一个map
                    Map<String, MembershipEvent> roomMemberEventMap = events
                            .stream()
                            .filter(e -> e instanceof MembershipEvent && StringUtils.equals(((MembershipEvent) e).getStateKey(), stateKey))
                            .map(e -> (MembershipEvent) e)
                            .sorted((o1, o2) -> o2.getStreamId().compareTo(o1.getStreamId()))
                            .collect(Collectors.toMap(MembershipEvent::getRoomId, e -> e, (o, n) -> o));

                    List<String> destRoomIds = Lists.newArrayList();
                    // 将符合条件的membership放入destRoomIds, 决定放入与否是逻辑交给predicate,predicate传入最新的membershipContent
                    roomMemberEventMap.forEach((roomId, roomMemberEvent) -> {
                        MembershipContent membershipContent = (MembershipContent) roomMemberEvent.getContent();
                        if (predicate.test(membershipContent)) {
                            destRoomIds.add(roomId);
                        }
                    });
                    return destRoomIds;
                });
    }
}
