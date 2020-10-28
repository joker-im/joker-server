package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * 包含 m.text, m.emote, m.notice
 */
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
@NoArgsConstructor

public class CommonTextContent extends AbstractMessageContent {

    private String format;

    private String formattedBody;
}
