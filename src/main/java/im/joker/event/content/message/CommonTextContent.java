package im.joker.event.content.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import im.joker.event.content.AbstractMessageContent;


/**
 * 包含 m.text, m.emote, m.notice
 */
public class CommonTextContent extends AbstractMessageContent {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String format;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String formattedBody;

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getFormattedBody() {
        return formattedBody;
    }

    public void setFormattedBody(String formattedBody) {
        this.formattedBody = formattedBody;
    }


}
