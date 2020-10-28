package im.joker.router;


import im.joker.api.vo.SyncRequest;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.SyncHandler;
import im.joker.helper.RequestProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0/", produces = MediaType.APPLICATION_JSON_VALUE)
public class SyncController {

    @Autowired
    private SyncHandler syncHandler;
    @Autowired
    private RequestProcessor requestProcessor;

    /**
     * https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-sync
     *
     * @param serverRequest
     * @return
     */
    @GetMapping("/sync")
    public Mono<ServerResponse> sync(ServerRequest serverRequest) {

        SyncRequest syncRequest = requestProcessor.parameterToBean(serverRequest, SyncRequest.class);
        Mono<IDevice> loginDeviceMono = Mono.subscriberContext().flatMap(context -> context.get(AuthFilter.getLoginDevice()));
        return loginDeviceMono.flatMap(e -> syncHandler.sync(syncRequest, e));

    }

    @PostMapping("/user/{userId}/filter")
    public Mono<ServerResponse> filter(@PathVariable String userId) {
        return syncHandler.filter(userId);
    }

}
