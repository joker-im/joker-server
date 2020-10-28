package im.joker.event.room;

import im.joker.event.content.state.MembershipContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UnsignedData {
    /**
     * 客户端发的时间, 与服务端接受的时间的时间差
     */
    private Long age;

    private String transactionId;

    private StrippedState inviteRoomState;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class StrippedState {

        private MembershipContent content;

        private String stateKey;

        private String type;

        private String sender;
    }

}
