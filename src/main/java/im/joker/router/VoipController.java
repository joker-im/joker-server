package im.joker.router;


import im.joker.handler.VoipHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequestMapping(path = "/_matrix/client/r0/", produces = MediaType.APPLICATION_JSON_VALUE)
public class VoipController {

    @Autowired
    private VoipHandler voipHandler;

    @GetMapping("/r0/voip/turnServer")
    public Mono<String> queryTurnServers() {
        return voipHandler.retrieveVoipServer();
    }
}
