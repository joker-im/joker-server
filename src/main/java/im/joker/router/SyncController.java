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
import org.springframework.web.bind.annotation.*;
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
     * 响应中会包含每个房间的prev_batch字段,能够通过/rooms/<room_id>/messages 作为其start参数拉取该房间的消息
     * 当没有since参数的时候, 每个相关的房间返回最近的聊天事件,并且返回在timeline第一条聊天事件之前的房间最初的状态,返回的next_batch作为下次调用sync的since
     * 当有since参数的时候,
     * 如果filter启用了lazy_load_members参数,timeline只会有对应发送人的membership事件,不包含不发送事件的sender的membership事件
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
