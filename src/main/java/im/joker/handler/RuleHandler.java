package im.joker.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RuleHandler {


    public Mono<ServerResponse> retrievePushRules(ServerRequest serverRequest) {
        return null;
    }
}
