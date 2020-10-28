package im.joker.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class VoipHandler {


    public Mono<String> retrieveVoipServer() {
        String result = """
                    {
                      "username": "1443779631:@user:example.com",
                      "password": "JlKfBy1QwLrO20385QyAtEyIv0=",
                      "uris": [
                        "turn:turn.example.com:3478?transport=udp",
                        "turn:10.20.30.40:3478?transport=tcp",
                        "turns:10.20.30.40:443?transport=tcp"
                      ],
                      "ttl": 86400
                    }
                """;

        return Mono.just(result);
    }
}
