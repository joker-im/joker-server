package im.joker.event.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@SuperBuilder
public abstract class AbstractMessageContent implements IContent {

    protected String body;

    @JsonProperty("msgtype")
    protected String msgType;
}
