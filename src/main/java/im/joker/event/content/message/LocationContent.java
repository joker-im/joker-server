package im.joker.event.content.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.IContent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class LocationContent implements IContent {

    private String body;

    private String geoUri;

    @JsonProperty("msgtype")
    private String msgType = "m.location";

    private LocationInfo info;


    @AllArgsConstructor
    @Data
    @NoArgsConstructor
    public static class LocationInfo {
        private String thumbnailUrl;

        private ThumbnailInfo thumbnailInfo;

    }
}
