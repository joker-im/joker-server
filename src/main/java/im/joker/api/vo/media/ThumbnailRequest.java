package im.joker.api.vo.media;

import javax.validation.constraints.NotNull;

public class ThumbnailRequest {

    private Integer width;

    private Integer height;

    private String method;

    private Boolean allowRemote;

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Boolean getAllowRemote() {
        return allowRemote;
    }

    public void setAllowRemote(Boolean allowRemote) {
        this.allowRemote = allowRemote;
    }
}