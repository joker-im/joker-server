package im.joker.event.room;

import java.util.List;

public class UnsignedData {
    /**
     * 客户端发的时间, 与服务端接受的时间的时间差
     */
    private Long age;

    private String transactionId;

    private List<AbstractRoomStateEvent> inviteRoomState;


    public Long getAge() {
        return age;
    }

    public void setAge(Long age) {
        this.age = age;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public List<AbstractRoomStateEvent> getInviteRoomState() {
        return inviteRoomState;
    }

    public void setInviteRoomState(List<AbstractRoomStateEvent> inviteRoomState) {
        this.inviteRoomState = inviteRoomState;
    }
}
