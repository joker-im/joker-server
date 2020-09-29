package im.joker.handler;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;


@Service
@Slf4j
public class RoomHandler {


    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Autowired
    private ReactiveMongoTemplate mongoTemplate;


    public Mono<ServerResponse> createRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> searchJoinedRooms(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> inviteToRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> joinRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> levelRoom(ServerRequest serverRequest) {
        return null;
    }

    public Mono<ServerResponse> kickRoom(ServerRequest serverRequest) {
        return null;
    }
}

