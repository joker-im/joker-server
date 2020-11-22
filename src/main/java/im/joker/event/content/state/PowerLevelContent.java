package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;

import java.util.Map;

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

    public static class RoomNotificationPower {
        private Integer room;

        public Integer getRoom() {
            return room;
        }

        public void setRoom(Integer room) {
            this.room = room;
        }
    }

    public Integer getBan() {
        return ban;
    }

    public void setBan(Integer ban) {
        this.ban = ban;
    }

    public Map<String, Integer> getEvents() {
        return events;
    }

    public void setEvents(Map<String, Integer> events) {
        this.events = events;
    }

    public Integer getEventDefault() {
        return eventDefault;
    }

    public void setEventDefault(Integer eventDefault) {
        this.eventDefault = eventDefault;
    }

    public Integer getInvite() {
        return invite;
    }

    public void setInvite(Integer invite) {
        this.invite = invite;
    }

    public Integer getKick() {
        return kick;
    }

    public void setKick(Integer kick) {
        this.kick = kick;
    }

    public Integer getRedact() {
        return redact;
    }

    public void setRedact(Integer redact) {
        this.redact = redact;
    }

    public Integer getStateDefault() {
        return stateDefault;
    }

    public void setStateDefault(Integer stateDefault) {
        this.stateDefault = stateDefault;
    }

    public Map<String, Integer> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Integer> users) {
        this.users = users;
    }

    public Integer getUserDefault() {
        return userDefault;
    }

    public void setUserDefault(Integer userDefault) {
        this.userDefault = userDefault;
    }

    public RoomNotificationPower getNotifications() {
        return notifications;
    }

    public void setNotifications(RoomNotificationPower notifications) {
        this.notifications = notifications;
    }
}
