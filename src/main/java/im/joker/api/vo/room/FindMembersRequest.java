package im.joker.api.vo.room;

public class FindMembersRequest {

    private String roomId;

    private String at;

    private String membership;

    private String notMembership;


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getAt() {
        return at;
    }

    public void setAt(String at) {
        this.at = at;
    }

    public String getMembership() {
        return membership;
    }

    public void setMembership(String membership) {
        this.membership = membership;
    }

    public String getNotMembership() {
        return notMembership;
    }

    public void setNotMembership(String notMembership) {
        this.notMembership = notMembership;
    }
}
