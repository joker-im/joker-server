package im.joker.helper;

import im.joker.device.DeviceManager;
import im.joker.session.AuthManager;
import im.joker.store.ReactiveMongodbStore;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Data
public class GlobalStateHolder {

    @Autowired
    private AuthManager authManager;
    @Autowired
    private DeviceManager deviceManager;
    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Autowired
    private ReactiveMongodbStore mongodbStore;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;
    @Autowired
    private LongPollingHelper longPollingHelper;
    @Autowired
    private EventSyncQueueManager eventSyncQueueManager;

}
