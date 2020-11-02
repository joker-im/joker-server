package im.joker.router;

import im.joker.api.vo.room.*;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.RoomHandler;
import im.joker.helper.RequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {

    @Autowired
    private RoomHandler roomHandler;
    @Autowired
    private RequestProcessor requestProcessor;


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

    @PostMapping("/rooms/{roomId}/kick")
    public Mono<Void> kick(@PathVariable String roomId, @RequestBody KickRequest kickRequest) {
        requestProcessor.validate(kickRequest);
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> roomHandler.kick(e, kickRequest, roomId));
    }


}
