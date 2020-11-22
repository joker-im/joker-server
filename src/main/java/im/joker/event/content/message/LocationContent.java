package im.joker.event.content.message;

import im.joker.event.content.AbstractMessageContent;
import im.joker.event.content.LocationInfo;

public class LocationContent extends AbstractMessageContent {

    private String geoUri;

    private LocationInfo info;


    public String getGeoUri() {
        return geoUri;
    }

    public void setGeoUri(String geoUri) {
        this.geoUri = geoUri;
    }

    public LocationInfo getInfo() {
        return info;
    }

    public void setInfo(LocationInfo info) {
        this.info = info;
    }
}
