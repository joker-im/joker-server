package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateContent extends AbstractStateContent {

    private String creator;

    private String roomVersion;

    /**
     * 这个用于客户端升级房间使用的
     */
    private PreviousRoom predecessor;

    /**
     * 其他服务器上的用户是否能加入本房间
     */
    private Boolean mFederate;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PreviousRoom {

        private String roomId;

        private String eventId;
    }
}
