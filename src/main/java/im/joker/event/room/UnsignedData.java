package im.joker.event.room;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UnsignedData {
    /**
     * 客户端发的时间, 与服务端接受的时间的时间差
     */
    private Long age;

    private String transactionId;

    private List<AbstractRoomStateEvent> inviteRoomState;


}
