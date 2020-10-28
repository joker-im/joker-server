package im.joker.event.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor

public class FileInfo {


    private String mimetype;

    private Integer size;

    private String thumbnailUrl;

    private ThumbnailInfo thumbnailInfo;
}
