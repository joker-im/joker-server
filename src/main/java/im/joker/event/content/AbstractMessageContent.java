package im.joker.event.content;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class AbstractMessageContent implements IContent {

    protected String body;

    @JsonProperty("msgtype")
    protected String msgType;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }
}
