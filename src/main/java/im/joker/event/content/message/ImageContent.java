package im.joker.event.content.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ImageContent implements IContent {

    private String body;

    private ImageInfo info;
    @JsonProperty("msgtype")
    private String msgType = "m.image";

    private String url;

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class ImageInfo {
        private Integer h;
        private Integer w;
        private String mimetype;
        private Integer size;
        private String thumbnailUrl;
        private ThumbnailInfo thumbnailInfo;
    }


}
