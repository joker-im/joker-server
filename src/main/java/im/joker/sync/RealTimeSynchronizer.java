package im.joker.sync;

import im.joker.api.vo.sync.SyncRequest;
import im.joker.device.IDevice;
import im.joker.event.room.IRoomEvent;
import im.joker.sync.entity.SyncResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 实时同步器，用来响应/sync请求
 *
 * @Author LinYuRong
 * @Date 2020/10/13 16:28
 * @Version 1.0
 */
public interface RealTimeSynchronizer {

    /**
     * 处理实时房间消息，需方便获取以下信息：
     * 1、房间最新消息
     * 2、房间实时状态
     * 3、用户实时状态
     *
     * @param event
     */
    Mono<Void> roomEventProcess(IRoomEvent event);

    /**
     * 响应/sync请求
     *
     * @param request
     * @return
     */
    Mono<SyncResponse> syncProcess(SyncRequest request, IDevice device);

    /**
     * 加载设备的所有活动房间详情
     *
     * @param deviceId
     * @return
     */
    Flux<String> getActiveRoomsOfDevice(String deviceId);


}
