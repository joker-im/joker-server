package im.joker.router;

import im.joker.helper.RoomSubscribeManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/versions", produces = MediaType.APPLICATION_JSON_VALUE)
public class VersionController {


    @Autowired
    private RoomSubscribeManager roomSubscribeManager;

    @GetMapping
    public Mono<String> version() {
        return Mono.just("{\"versions\":[\"r0.6.0\"],\"unstable_features\":{\"org.matrix.label_based_filtering\":true,\"org.matrix.e2e_cross_signing\":true,\"org.matrix.msc2432\":true,\"uk.half-shot.msc2666\":true,\"io.element.e2ee_forced.public\":false,\"io.element.e2ee_forced.private\":false,\"io.element.e2ee_forced.trusted_private\":false}}");
    }


    @GetMapping("/test")
    public Flux<String> test(@RequestParam("deviceId") String deviceId) {
        return Flux.empty();
    }
}
