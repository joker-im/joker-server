package org.im.joker.router;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class AccountHandler {
    public Mono<ServerResponse> register(ServerRequest serverRequest) {
        return null;
    }
}
