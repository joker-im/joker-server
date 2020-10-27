package im.joker.event.content.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * 包含 m.text, m.emote, m.notice
 */
@AllArgsConstructor
@Data
@NoArgsConstructor

public class CommonTextContent implements IContent {

    private String body;

    @JsonProperty("msgtype")
    private String msgType;

    private String format;

    private String formattedBody;
}
