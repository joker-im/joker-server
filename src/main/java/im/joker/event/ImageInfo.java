package im.joker.event;

import im.joker.event.content.ThumbnailInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ImageInfo {
    private Integer h;
    private Integer w;
    private String mimetype;
    private Integer size;
    private String thumbnailUrl;
    private ThumbnailInfo thumbnailInfo;
}
