package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import reactor.core.publisher.Mono;

/**
 * @author linyurong
 * @date 2020/9/30 9:49
 */
public interface ISyncManager {

    /**
     * sync
     *
     * @param syncRequest
     * @return
     */
    Mono<String> sync(SyncRequest syncRequest);
}
