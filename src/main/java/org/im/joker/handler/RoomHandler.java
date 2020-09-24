package org.im.joker.handler;

import org.im.joker.error.ErrorCode;
import org.im.joker.exception.JokerImException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class RoomHandler {


    public Mono<ServerResponse> createRoom(ServerRequest serverRequest) {
        return ServerResponse.ok().build().map(e -> {
            throw new JokerImException(ErrorCode.UNRECOGNIZED, HttpStatus.BAD_REQUEST);
        });
    }

    public Mono<ServerResponse> searchJoinedRooms(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> inviteToRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> joinRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> levelRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> kickRoom(ServerRequest serverRequest) {
        return null;
    }
}

