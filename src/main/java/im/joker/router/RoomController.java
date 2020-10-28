package im.joker.router;

import im.joker.api.vo.room.*;
import im.joker.config.filter.AuthFilter;
import im.joker.handler.RoomHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0/", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoomController {

    @Autowired
    private RoomHandler roomHandler;


    /**
     * 创建房间 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
     *
     * @param roomRequest
     * @return
     */
    @PostMapping("/createRoom")
    public Mono<CreateRoomResponse> createRoom(@RequestBody CreateRoomRequest roomRequest) {
        return Mono.subscriberContext()
                .flatMap(context -> roomHandler.createRoom(context.get(AuthFilter.getLoginDevice()), roomRequest));
    }

    /**
     * 返回当前用户的房间列表 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
     *
     * @return
     */
    @GetMapping("/joined_rooms")
    public Mono<JoinedRoomResponse> searchJoinedRooms() {
        return Mono.subscriberContext()
                .flatMap(context -> roomHandler.searchJoinedRooms(context.get(AuthFilter.getLoginDevice())));
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
        return Mono.subscriberContext()
                .flatMap(context -> roomHandler.inviteToRoom(roomId, inviteRequest, context.get(AuthFilter.getLoginDevice())));
    }

    /**
     * 用户参与进某个房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-join
     *
     * @param roomId
     * @return
     */
    @PostMapping("/rooms/{roomId}/join")
    public Mono<JoinRoomResponse> joinRoom(@PathVariable String roomId) {
        return Mono.subscriberContext()
                .flatMap(context -> roomHandler.joinRoom(context.get(AuthFilter.getLoginDevice()), roomId));
    }


    /**
     * 用户离开房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
     *
     * @param roomId
     * @return
     */
    @PostMapping("/rooms/{roomId}/leave")
    public Mono<Void> leaveRoom(@PathVariable String roomId) {
        return Mono.subscriberContext()
                .flatMap(context -> roomHandler.levelRoom(context.get(AuthFilter.getLoginDevice()), roomId));
    }


}
