package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;


/**
 * 包含 m.text, m.emote, m.notice
 */
public class CommonTextContent extends AbstractMessageContent {

    private String format;

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
