package im.joker.event.content;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AudioInfo {

    private Integer duration;

    private String mimetype;

    private Integer size;
}
