package im.joker.api.vo.room;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadMarkerRequest {

    @JsonProperty("m.fully_read")
    private String fullRead;
    @JsonProperty("m.read")
    private String read;

    public String getFullRead() {
        return fullRead;
    }

    public void setFullRead(String fullRead) {
        this.fullRead = fullRead;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
