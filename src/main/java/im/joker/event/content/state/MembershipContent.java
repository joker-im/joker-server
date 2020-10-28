package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;
import im.joker.event.room.UnsignedData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * 就是文档的EventContent类型
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class MembershipContent implements IContent {

    private String avatarUrl;

    @JsonProperty("displayname")
    private String displayName;


    /**
     * ["invite", "join", "knock", "leave", "ban"] 必须为这其一
     */
    @NotNull
    private String membership;


    private Boolean isDirect;


}
