package org.im.joker.router;


import org.im.joker.handler.AccountHandler;
import org.im.joker.handler.RoomHandler;
import org.im.joker.handler.SyncHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class JokerImRouter {


    @Bean
    public RouterFunction<ServerResponse> routeRoom(RoomHandler roomHandler) {

        return RouterFunctions
                // 创建房间 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
                .route(POST("/_matrix/client/r0/createRoom")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::createRoom)
                // 返回当前用户的房间列表 #https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-joined-rooms
                .andRoute(GET("/_matrix/client/r0/joined_rooms")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::searchJoinedRooms)
                // 邀请用户进去某个房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-invite
                .andRoute(POST("/_matrix/client/r0/rooms/{roomId}/invite")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::inviteToRoom)
                // 用户参与进某个房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-join
                .andRoute(POST("/_matrix/client/r0/rooms/{roomId}/join")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::joinRoom)
                // 用户离开房间 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-leave
                .andRoute(POST("/_matrix/client/r0/rooms/{roomId}/leave")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::levelRoom)
                // 要有足够的权限才能T人 #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-rooms-roomid-kick
                .andRoute(POST("/_matrix/client/r0/rooms/{roomId}/kick")
                        .and(accept(MediaType.APPLICATION_JSON)), roomHandler::kickRoom);

    }

    @Bean
    public RouterFunction<ServerResponse> routeAccount(AccountHandler accountHandler) {
        // 注册一个用户,会返回access_token和device_id #https://matrix.org/docs/spec/client_server/r0.6.1#post-matrix-client-r0-register
        return RouterFunctions
                .route(POST("/_matrix/client/r0/register")
                        .and(accept(MediaType.APPLICATION_JSON)), accountHandler::register)
                .andRoute(POST("/_matrix/client/r0/register")
                        .and(accept(MediaType.APPLICATION_JSON)), accountHandler::register)
                .andRoute(POST("/_matrix/client/r0/register")
                        .and(accept(MediaType.APPLICATION_JSON)), accountHandler::register)
                .andRoute(POST("/_matrix/client/r0/register")
                        .and(accept(MediaType.APPLICATION_JSON)), accountHandler::register)
                .andRoute(POST("/_matrix/client/r0/register")
                        .and(accept(MediaType.APPLICATION_JSON)), accountHandler::register)


                ;


    }

    @Bean
    public RouterFunction<ServerResponse> routeSync(SyncHandler syncHandler) {
        return RouterFunctions
                // sync  https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-sync
                .route(GET("/_matrix/client/r0/sync"), syncHandler::sync)


                ;
    }


}
