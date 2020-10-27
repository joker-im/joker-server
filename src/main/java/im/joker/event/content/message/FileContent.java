package im.joker.event.content.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class FileContent implements IContent {

    private String body;

    private String filename;

    private FileInfo info;
    @JsonProperty("msgtype")
    private String msgType = "m.file";

    private String url;


    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class FileInfo {

        private String mimetype;

        private Integer size;

        private String thumbnailUrl;

        private ThumbnailInfo thumbnailInfo;
    }

}

