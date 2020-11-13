package im.joker.helper;

import im.joker.event.room.AbstractRoomEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Component
public class EventQueueManager {

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;
    @Autowired
    private RoomSubscribeManager roomSubscribeManager;


    /**
     * 从对应的设备中到自己关心的队列里,拿出对应限制条数的消息
     * key是房间, value是此房间的事件集
     *
     * @param deviceId
     * @param limitOfRoom
     * @return
     */
    public Flux<Map<String, List<AbstractRoomEvent>>> takeRelatedEvents(String deviceId, int limitOfRoom) {

        return null;
    }



}
