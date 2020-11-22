package im.joker.event.content.message;

import im.joker.event.ImageInfo;
import im.joker.event.content.AbstractMessageContent;
public class ImageContent extends AbstractMessageContent {

    private ImageInfo info;

    private String url;


    public ImageInfo getInfo() {
        return info;
    }

    public void setInfo(ImageInfo info) {
        this.info = info;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
