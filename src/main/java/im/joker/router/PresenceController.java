package im.joker.router;

import im.joker.api.vo.PresenceRequest;
import im.joker.config.filter.AuthFilter;
import im.joker.device.IDevice;
import im.joker.handler.PresenceHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0/", produces = MediaType.APPLICATION_JSON_VALUE)
public class PresenceController {

    @Autowired
    private PresenceHandler presenceHandler;

    @PostMapping("/presence/{userId}/status")
    public Mono<Void> setPresence(@PathVariable String userId, @RequestBody PresenceRequest presenceRequest) {
        Mono<IDevice> loginDevice = Mono.subscriberContext().flatMap(e -> e.get(AuthFilter.getLoginDevice()));
        return loginDevice.flatMap(iDevice -> presenceHandler.setPresence(presenceRequest, iDevice));
    }

}
