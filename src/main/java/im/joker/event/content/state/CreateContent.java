package im.joker.event.content.state;

import com.fasterxml.jackson.annotation.JsonProperty;
import im.joker.event.content.AbstractStateContent;

public class CreateContent extends AbstractStateContent {

    private String creator;

    private String roomVersion;

    /**
     * 这个用于客户端升级房间使用的
     */
    private PreviousRoom predecessor;

    /**
     * 其他服务器上的用户是否能加入本房间
     */
    @JsonProperty("m.federate")
    private Boolean mFederate;


    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getRoomVersion() {
        return roomVersion;
    }

    public void setRoomVersion(String roomVersion) {
        this.roomVersion = roomVersion;
    }

    public PreviousRoom getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(PreviousRoom predecessor) {
        this.predecessor = predecessor;
    }

    public Boolean getMFederate() {
        return mFederate;
    }

    public void setMFederate(Boolean mFederate) {
        this.mFederate = mFederate;
    }

    public static class PreviousRoom {

        private String roomId;

        private String eventId;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getEventId() {
            return eventId;
        }

        public void setEventId(String eventId) {
            this.eventId = eventId;
        }
    }
}
