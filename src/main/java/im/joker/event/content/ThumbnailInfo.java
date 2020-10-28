package im.joker.event.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ThumbnailInfo {

    private Integer h;
    private Integer w;
    private String mimetype;
    private Integer size;
}
