package im.joker.event.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class LocationInfo {

    private String thumbnailUrl;

    private ThumbnailInfo thumbnailInfo;
}
