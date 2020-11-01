package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class PowerLevelContent extends AbstractStateContent {

    /**
     * 用户在这个房间的权限高于此才能禁止别人说话
     */
    private Integer ban;

    /**
     * 指定发送具体某些事件的权限值, user_id在此房间要高于此值才能发对应的事件.优先级最高
     * key是事件类型,value是power
     */
    private Map<String, Integer> events;

    /**
     * 消息事件(非状态事件) 发送至少需要的权限值
     */
    private Integer eventDefault;
    /**
     * 邀请人至少需要的权限值
     */
    private Integer invite;
    /**
     * T人至少需要的权限值
     */
    private Integer kick;
    /**
     * 编辑房间事件至少需要的权限值
     */
    private Integer redact;
    /**
     * 状态事件(非消息事件)发送至少需要的权限值
     */
    private Integer stateDefault;

    /**
     * 设置某些用户的权限值
     * key是用户id,value是power
     */
    private Map<String, Integer> users;
    /**
     * 每个用户默认的权限值
     */
    private Integer userDefault;
    /**
     * 某些类型的通知需要的权限值, 目前只有房间通知
     */
    private RoomNotificationPower notifications;

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Builder
    public static class RoomNotificationPower {
        private Integer room;
    }


}
