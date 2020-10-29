package im.joker.sync;

import im.joker.event.room.IRoomEvent;
import im.joker.sync.entity.SyncResponse;
import lombok.Data;

/**
 * @Author LinYuRong
 * @Date 2020/10/29 18:17
 * @Version 1.0
 */
@Data
public class SyncRoomEventAdder {
    private boolean existsData;

    public void addEvent(IRoomEvent event) {

    }

    public SyncResponse get() {
        SyncResponse syncResponse = new SyncResponse();
        syncResponse.setNextBatch("test");
        return syncResponse;
    }
}
