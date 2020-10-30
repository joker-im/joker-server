package im.joker.router;


import im.joker.api.vo.sync.FilterResponse;
import im.joker.api.vo.sync.SyncRequest;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.SyncHandler;
import im.joker.helper.RequestProcessor;
import im.joker.sync.entity.SyncResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0", produces = MediaType.APPLICATION_JSON_VALUE)
public class SyncController {

    @Autowired
    private SyncHandler syncHandler;
    @Autowired
    private RequestProcessor requestProcessor;

    /**
     * https://matrix.org/docs/spec/client_server/r0.6.1#get-matrix-client-r0-sync
     *
     * @param param
     * @return
     */
    @GetMapping("/sync")
    public Mono<SyncResponse> sync(@RequestParam Map<String, Object> param) {
        SyncRequest syncRequest = requestProcessor.convert(param, SyncRequest.class);
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(context -> Mono.just(context.get(AuthFilter.getLoginDevice())));
        return loginDevice.flatMap(e -> syncHandler.sync(syncRequest, e));
    }

    @PostMapping("/user/{userId}/filter")
    public Mono<FilterResponse> filter(@PathVariable String userId) {
        return syncHandler.filter(userId);
    }

}
