package im.joker.api.vo.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.api.vo.e2e.DeviceLists;
import im.joker.api.vo.e2e.ToDevice;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import im.joker.event.room.other.FullReadMarkerEvent;

import java.util.List;
import java.util.Map;

public class SyncResponse {

    /**
     * 下次用的sinceId
     */
    private String nextBatch;

    /**
     * 主要返回的聊天事件和状态事件
     */
    private Rooms rooms;

    /**
     * 用于更新别人的状态
     */
    private Presence presence;

    /**
     * 用于推送到一组设备
     */
    private ToDevice toDevice;

    /**
     * 端到端加密专用
     */
    private DeviceLists deviceLists;

    /**
     * 端到端加密专用
     */
    private Map<String, Integer> deviceOneTimeKeysCount;


    public String getNextBatch() {
        return nextBatch;
    }

    public void setNextBatch(String nextBatch) {
        this.nextBatch = nextBatch;
    }

    public Rooms getRooms() {
        return rooms;
    }

    public void setRooms(Rooms rooms) {
        this.rooms = rooms;
    }

    public Presence getPresence() {
        return presence;
    }

    public void setPresence(Presence presence) {
        this.presence = presence;
    }

    public ToDevice getToDevice() {
        return toDevice;
    }

    public void setToDevice(ToDevice toDevice) {
        this.toDevice = toDevice;
    }

    public DeviceLists getDeviceLists() {
        return deviceLists;
    }

    public void setDeviceLists(DeviceLists deviceLists) {
        this.deviceLists = deviceLists;
    }

    public Map<String, Integer> getDeviceOneTimeKeysCount() {
        return deviceOneTimeKeysCount;
    }

    public void setDeviceOneTimeKeysCount(Map<String, Integer> deviceOneTimeKeysCount) {
        this.deviceOneTimeKeysCount = deviceOneTimeKeysCount;
    }

    public static class Presence {
        private List<AbstractRoomEvent> events;

        public List<AbstractRoomEvent> getEvents() {
            return events;
        }

        public void setEvents(List<AbstractRoomEvent> events) {
            this.events = events;
        }
    }


    public static class Rooms {
        private Map<String, JoinedRooms> join;
        private Map<String, InvitedRooms> invite;
        private Map<String, LeftRooms> leave;

        public Map<String, JoinedRooms> getJoin() {
            return join;
        }

        public void setJoin(Map<String, JoinedRooms> join) {
            this.join = join;
        }

        public Map<String, InvitedRooms> getInvite() {
            return invite;
        }

        public void setInvite(Map<String, InvitedRooms> invite) {
            this.invite = invite;
        }

        public Map<String, LeftRooms> getLeave() {
            return leave;
        }

        public void setLeave(Map<String, LeftRooms> leave) {
            this.leave = leave;
        }
    }

    public static class InvitedRooms {
        private State inviteState;

        public State getInviteState() {
            return inviteState;
        }

        public void setInviteState(State inviteState) {
            this.inviteState = inviteState;
        }
    }


    public static class LeftRooms {
        private State state;
        private Timeline timeline;
        private AccountData accountData;

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public Timeline getTimeline() {
            return timeline;
        }

        public void setTimeline(Timeline timeline) {
            this.timeline = timeline;
        }

        public AccountData getAccountData() {
            return accountData;
        }

        public void setAccountData(AccountData accountData) {
            this.accountData = accountData;
        }
    }

    public static class JoinedRooms {
        /**
         * 客户端需要正确渲染给用户的房间概要信息
         */
        private RoomSummary summary;
        /**
         * 房间状态表示, 本次since -- 本次同步的nextBatch这之间的事件,其中会包含timeline事件.state里的事件就是截止timeline的首条事件之间的当时最新状态事件
         */
        private State state;

        /**
         * 包含消息事件或者状态事件
         */
        private Timeline timeline;

        /**
         * 顺时事件. 比如m.typing
         */
        private Ephemeral ephemeral;

        /**
         * 当前用户在这个房间存的私有数据
         */
        private AccountData accountData;

        /**
         * 对该房间的未读通知
         */
        private UnreadNotificationCount unreadNotification;


        public RoomSummary getSummary() {
            return summary;
        }

        public void setSummary(RoomSummary summary) {
            this.summary = summary;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }

        public Timeline getTimeline() {
            return timeline;
        }

        public void setTimeline(Timeline timeline) {
            this.timeline = timeline;
        }

        public Ephemeral getEphemeral() {
            return ephemeral;
        }

        public void setEphemeral(Ephemeral ephemeral) {
            this.ephemeral = ephemeral;
        }

        public AccountData getAccountData() {
            return accountData;
        }

        public void setAccountData(AccountData accountData) {
            this.accountData = accountData;
        }

        public UnreadNotificationCount getUnreadNotification() {
            return unreadNotification;
        }

        public void setUnreadNotification(UnreadNotificationCount unreadNotification) {
            this.unreadNotification = unreadNotification;
        }
    }

    public static class RoomSummary {
        /**
         * 用于房间的起名. 如果房间没有m.room.name或者 m.room.canonical_alias这些事件
         */
        @JsonProperty("m.heroes")
        private List<String> heroes;

        /**
         * 加入此房间的成员数 (包含自己, 这个字段起码要返回过一次,变动时候也要放回)
         */
        @JsonProperty("m.joined_member_count")
        private Integer joinedMemberCount;

        /**
         * 被邀请进次房间的成员数(包含自己, 这个字段起码要返回过一次,变动时候也要放回)
         */
        @JsonProperty("m.invited_member_count")
        private Integer invitedMemberCount;

        public List<String> getHeroes() {
            return heroes;
        }

        public void setHeroes(List<String> heroes) {
            this.heroes = heroes;
        }

        public Integer getJoinedMemberCount() {
            return joinedMemberCount;
        }

        public void setJoinedMemberCount(Integer joinedMemberCount) {
            this.joinedMemberCount = joinedMemberCount;
        }

        public Integer getInvitedMemberCount() {
            return invitedMemberCount;
        }

        public void setInvitedMemberCount(Integer invitedMemberCount) {
            this.invitedMemberCount = invitedMemberCount;
        }
    }

    public static class State {
        private List<AbstractRoomStateEvent> events;

        public List<AbstractRoomStateEvent> getEvents() {
            return events;
        }

        public void setEvents(List<AbstractRoomStateEvent> events) {
            this.events = events;
        }
    }

    public static class Timeline {
        private List<AbstractRoomEvent> events;
        /**
         * 就是拉取房间消息的from参数
         */
        private String prevBatch;
        private Boolean limited;

        public List<AbstractRoomEvent> getEvents() {
            return events;
        }

        public void setEvents(List<AbstractRoomEvent> events) {
            this.events = events;
        }

        public String getPrevBatch() {
            return prevBatch;
        }

        public void setPrevBatch(String prevBatch) {
            this.prevBatch = prevBatch;
        }

        public Boolean getLimited() {
            return limited;
        }

        public void setLimited(Boolean limited) {
            this.limited = limited;
        }
    }

    public static class Ephemeral {
        private List<AbstractRoomEvent> events;

        public List<AbstractRoomEvent> getEvents() {
            return events;
        }

        public void setEvents(List<AbstractRoomEvent> events) {
            this.events = events;
        }
    }

    public static class AccountData {
        private List<FullReadMarkerEvent> events;

        public List<FullReadMarkerEvent> getEvents() {
            return events;
        }

        public void setEvents(List<FullReadMarkerEvent> events) {
            this.events = events;
        }
    }


    public static class UnreadNotificationCount {
        /**
         * 当前房间未读通知高亮的标志
         */
        private Integer highlightCount;
        /**
         * 总共在这个房间未读的消息数
         */
        private Integer notificationCount;

        public Integer getHighlightCount() {
            return highlightCount;
        }

        public void setHighlightCount(Integer highlightCount) {
            this.highlightCount = highlightCount;
        }

        public Integer getNotificationCount() {
            return notificationCount;
        }

        public void setNotificationCount(Integer notificationCount) {
            this.notificationCount = notificationCount;
        }
    }
}
