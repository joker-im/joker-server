package im.joker.handler;

import im.joker.api.vo.sync.FilterResponse;
import im.joker.api.vo.sync.SyncRequest;
import im.joker.device.IDevice;
import im.joker.sync.IRealTimeSynchronizer;
import im.joker.sync.entity.SyncResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@Service
@Slf4j
public class SyncHandler {

    @Autowired
    private IRealTimeSynchronizer realTimeSynchronizer;

    public Mono<SyncResponse> sync(SyncRequest syncRequest, IDevice device) {
        return realTimeSynchronizer.syncProcess(syncRequest, device)
                .timeout(Duration.ofSeconds(30), Mono.just(new SyncResponse()));
    }


    public Mono<FilterResponse> filter(String userId) {
        return Mono.just(FilterResponse.builder().filterId(UUID.randomUUID().toString()).build());
    }

}
