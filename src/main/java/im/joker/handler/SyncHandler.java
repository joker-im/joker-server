package im.joker.handler;

import im.joker.api.vo.FilterResponse;
import im.joker.config.filter.AuthFilter;
import im.joker.device.Device;
import im.joker.device.IDevice;
import lombok.extern.slf4j.Slf4j;
import im.joker.api.vo.SyncRequest;
import im.joker.helper.RequestProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Filter;

@Service
@Slf4j
public class SyncHandler {

    @Autowired
    private RequestProcessor requestProcessor;

    private Map<String, MonoSink<Boolean>> monoSinkMap = new ConcurrentHashMap<>();


    public Mono<ServerResponse> sync(SyncRequest syncRequest, IDevice loginDevice) {
        return Mono.create((Consumer<MonoSink<Boolean>>) monoSink -> monoSinkMap.put(loginDevice.getDeviceId(), monoSink))
                .flatMap(e -> ServerResponse.ok().build())
                .timeout(Duration.ofSeconds(30), ServerResponse.notFound().build());
    }


    public Mono<ServerResponse> filter(String userId) {
        return Mono.just(FilterResponse.builder().filterId(UUID.randomUUID().toString()).build())
                .flatMap(e ->
                        ServerResponse.ok().bodyValue(e)
                );
    }

}
