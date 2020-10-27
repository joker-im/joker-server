package im.joker.event.content.message;

import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class VideoContent implements IContent {

    private String body;

    private VideoInfo info;

    private String msgType = "m.video";

    private String url;


    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class VideoInfo {

        private Integer duration;

        private Integer h;

        private Integer w;

        private String mimetype;

        private Integer size;

        private String thumbnailUrl;

        private ThumbnailInfo thumbnailInfo;

    }
}
