package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
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
    @JsonProperty("m.federate")
    private Boolean mFederate;


    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class PreviousRoom {

        private String roomId;

        private String eventId;
    }
}
