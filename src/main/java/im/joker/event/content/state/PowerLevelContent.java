package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import im.joker.event.content.IContent;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PowerLevelContent extends AbstractStateContent {

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
