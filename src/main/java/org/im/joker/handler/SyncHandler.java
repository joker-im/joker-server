package org.im.joker.handler;

import lombok.extern.slf4j.Slf4j;
import org.im.joker.api.SyncRequest;
import org.im.joker.assist.RequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class SyncHandler {

    @Autowired
    private RequestProcessor requestProcessor;


    public Mono<ServerResponse> sync(ServerRequest serverRequest) {
        return requestProcessor.parameterToBean(serverRequest, SyncRequest.class)
                .flatMap(e -> ServerResponse.ok().bodyValue(e));
    }


}
