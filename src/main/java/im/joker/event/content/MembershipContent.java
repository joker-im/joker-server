package im.joker.event.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MembershipContent implements IContent {

    private String avatarUrl;

    @JsonProperty("displayname")
    private String displayName;


    /**
     * ["invite", "join", "knock", "leave", "ban"]
     */
    @NotNull
    private String membership;


    private Boolean isDirect;


}
