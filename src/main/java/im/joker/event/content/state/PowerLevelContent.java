package im.joker.event.content.state;

import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PowerLevelContent implements IContent {

    private Integer ban;

    /**
     * key是事件类型,value是power
     */
    private Map<String, Integer> events;

    private Integer eventDefault;

    private Integer invite;

    private Integer kick;

    private Integer redact;

    private Integer stateDefault;

    /**
     * key是用户名称,value是power
     */
    private Map<String, Integer> users;

    private Integer userDefault;

    private RoomNotificationPower notifications;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class RoomNotificationPower {
        private Integer room;
    }


}
