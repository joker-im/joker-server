package im.joker.event.content;

import im.joker.event.content.ThumbnailInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class VideoInfo {
    private Integer duration;

    private Integer h;

    private Integer w;

    private String mimetype;

    private Integer size;

    private String thumbnailUrl;

    private ThumbnailInfo thumbnailInfo;
}
