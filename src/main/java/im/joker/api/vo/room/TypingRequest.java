package im.joker.api.vo.room;

public class TypingRequest {

    private String roomId;

    private Boolean typing;

    private Integer timeout;

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public Boolean getTyping() {
        return typing;
    }

    public void setTyping(Boolean typing) {
        this.typing = typing;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
