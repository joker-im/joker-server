package im.joker.api.vo.presence;

public class PresenceRequest {

    /**
     * ["online", "offline", "unavailable"]  只能为其中一种
     */
    private String presence;

    private String statusMsg;


    public String getPresence() {
        return presence;
    }

    public void setPresence(String presence) {
        this.presence = presence;
    }

    public String getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(String statusMsg) {
        this.statusMsg = statusMsg;
    }
}
