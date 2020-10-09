package im.joker.event.room;

import im.joker.event.EventType;

import java.io.Serializable;

/**
 * 房间成员事件，加入和退出等。
 *
 * @author linyurong
 */
public class RoomMembershipEvent extends AbstractRoomEvent<RoomMembershipEvent.Content> {

    public static class Content implements Serializable {
        private String membership;
        private String displayName;
        private String avatarUrl;

        public Content() {}

        public Content(String membership, String displayName, String avatarUrl) {
            this.membership = membership;
            this.displayName = displayName;
            this.avatarUrl = avatarUrl;
        }

        public String getMembership() {
            return membership;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setMembership(String membership) {
            this.membership = membership;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

    public RoomMembershipEvent() {}

    public RoomMembershipEvent(String sender, String membership, String target, String displayName, String avatarUrl) {
        super(EventType.Membership.get(), sender, System.currentTimeMillis(), new Content(membership, displayName, avatarUrl));
        this.stateKey = target;
    }

    public String getMembership() {
        return getContent().getMembership();
    }
}
