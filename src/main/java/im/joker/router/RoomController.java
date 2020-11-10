package im.joker.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import im.joker.api.vo.room.*;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.event.EventType;
import im.joker.event.ImEvent;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.IRoomEvent;
import im.joker.event.room.message.MessageEvent;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.handler.RoomHandler;
import im.joker.helper.RequestProcessor;
import im.joker.util.GsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {

    @Autowired
    private RoomHandler roomHandler;
    @Autowired
    private RequestProcessor requestProcessor;
    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 创建房间 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
     *
     * @param roomRequest
     * @return
     */
    @PostMapping("/createRoom")
    public Mono<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest roomRequest) {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice
                .flatMap(e -> roomHandler.createRoom(e, roomRequest));
    }

    /**
     * 返回当前用户的房间列表 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
     *
     * @return
     */
    @GetMapping("/joined_rooms")
    public Mono<JoinedRoomResponse> searchJoinedRooms() {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice
                .flatMap(e -> roomHandler.searchJoinedRooms(e));
    }

    /**
     * 邀请用户进去某个房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-invite
     *
     * @param roomId
     * @param inviteRequest
     * @return
     */
    @PostMapping("/rooms/{roomId}/invite")
    public Mono<Void> inviteToRoom(@PathVariable String roomId, @RequestBody InviteRequest inviteRequest) {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> roomHandler.inviteToRoom(roomId, inviteRequest, e));
    }

    /**
     * 用户参与进某个房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-join
     *
     * @param roomId
     * @return
     */
    @PostMapping("/rooms/{roomId}/join")
    public Mono<JoinRoomResponse> joinRoom(@PathVariable String roomId) {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));

        return loginDevice.flatMap(e -> roomHandler.joinRoom(e, roomId));
    }


    /**
     * 用户离开房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
     *
     * @param roomId
     * @return
     */
    @PostMapping("/rooms/{roomId}/leave")
    public Mono<Void> leaveRoom(@PathVariable String roomId) {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> roomHandler.levelRoom(e, roomId));
    }


    /**
     * 将某个用户T出房间
     *
     * @param roomId
     * @param kickRequest
     * @return
     */
    @PostMapping("/rooms/{roomId}/kick")
    public Mono<Void> kick(@PathVariable String roomId, @RequestBody KickRequest kickRequest) {
        requestProcessor.validate(kickRequest);
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> roomHandler.kick(e, kickRequest, roomId));
    }


    /**
     * 发送房间消息事件
     *
     * @param roomId
     * @param eventType
     * @param txId
     * @param jsonBody  这个是MessageEvent的Content
     * @return
     */
    @PutMapping("/rooms/{roomId}/send/{eventType}/{txnId}")
    public Mono<EventIdResponse> sendMessageEvent(@PathVariable("roomId") String roomId,
                                                  @PathVariable("eventType") String eventType,
                                                  @PathVariable("txnId") String txId,
                                                  @RequestBody String jsonBody) {
        EventType type = EventType.findByType(eventType);
        if (type == null || type.isState()) {
            return Mono.error(new ImException(ErrorCode.INVALID_PARAM, HttpStatus.BAD_REQUEST, "无法识别次事件类型"));
        }
        JsonObject contentObject = GsonUtils.get().fromJson(jsonBody, JsonObject.class);
        JsonObject eventObject = new JsonObject();
        eventObject.addProperty("type", eventType);
        eventObject.add("content", contentObject);
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return Mono.create((MonoSink<AbstractRoomEvent> monoSink) -> {
            try {
                AbstractRoomEvent event = objectMapper.reader().forType(type.getEventClass()).readValue(eventObject.toString());
                event.setRoomId(roomId);
                event.setTransactionId(txId);
                monoSink.success(event);
            } catch (JsonProcessingException e) {
                log.error("转换json报错了", e);
                monoSink.error(new ImException(ErrorCode.UNRECOGNIZED, HttpStatus.BAD_REQUEST));
            }
        }).zipWith(loginDevice)
                .flatMap(tuple2 -> roomHandler.sendMessageEvent(tuple2.getT2(), tuple2.getT1()))
                .map(e -> EventIdResponse.builder().eventId(e).build());
    }


}
