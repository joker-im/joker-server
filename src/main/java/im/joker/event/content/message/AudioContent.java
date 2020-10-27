package im.joker.event.content.message;

import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AudioContent implements IContent {

    private String body;

    private String msgType = "m.audio";

    private String url;

    private AudioInfo info;

    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class AudioInfo {

        private Integer duration;

        private String mimetype;

        private Integer size;
    }
    
}
