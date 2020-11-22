package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.AudioInfo;

public class AudioContent extends AbstractMessageContent {


    private String url;

    private AudioInfo info;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AudioInfo getInfo() {
        return info;
    }

    public void setInfo(AudioInfo info) {
        this.info = info;
    }
}
