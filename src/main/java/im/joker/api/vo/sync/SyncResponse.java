package im.joker.api.vo.sync;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.api.vo.e2e.DeviceLists;
import im.joker.api.vo.e2e.ToDevice;
import im.joker.event.room.AbstractRoomEvent;
import im.joker.event.room.AbstractRoomStateEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
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

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class Presence {
        private List<AbstractRoomEvent> events;
    }


    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class Rooms {
        private Map<String, JoinedRooms> join;
        private Map<String, InvitedRooms> invite;
        private Map<String, LeftRooms> leave;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class InvitedRooms {
        private State inviteState;
    }


    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class LeftRooms {
        private State state;
        private Timeline timeline;
        private AccountData accountData;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
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

    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
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
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class State {
        private List<AbstractRoomStateEvent> events;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class Timeline {
        private List<AbstractRoomEvent> events;
        private String prevBatch;
        private Boolean limited;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class Ephemeral {
        private List<AbstractRoomEvent> events;
    }

    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    private static class AccountData {
        private List<AbstractRoomEvent> events;
    }


    @AllArgsConstructor
    @Data
    @Builder
    @NoArgsConstructor
    public static class UnreadNotificationCount {
        /**
         * 当前房间未读通知高亮的标志
         */
        private Integer highlightCount;
        /**
         * 总共在这个房间未读的消息数
         */
        private Integer notificationCount;

    }
}
