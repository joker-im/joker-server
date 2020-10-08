package im.joker.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionHandler {
    public Mono<ServerResponse> versions(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"versions\":[\"r0.6.0\"],\"unstable_features\":{\"org.matrix.label_based_filtering\":true,\"org.matrix.e2e_cross_signing\":true,\"org.matrix.msc2432\":true,\"uk.half-shot.msc2666\":true,\"io.element.e2ee_forced.public\":false,\"io.element.e2ee_forced.private\":false,\"io.element.e2ee_forced.trusted_private\":false}}");
    }
}
