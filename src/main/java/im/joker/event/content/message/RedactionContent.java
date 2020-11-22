package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;

public class RedactionContent extends AbstractMessageContent {

    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
