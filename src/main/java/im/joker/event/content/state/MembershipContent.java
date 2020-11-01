package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;
import lombok.*;

import javax.validation.constraints.NotNull;

/**
 * 就是文档的EventContent类型
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MembershipContent extends AbstractStateContent {

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
