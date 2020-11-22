package im.joker.event.content.state;

import im.joker.event.content.AbstractStateContent;

public class GuestAccessContent extends AbstractStateContent {

    /**
     * 只能为以下["can_join", "forbidden"]
     */
    private String guestAccess;

    public String getGuestAccess() {
        return guestAccess;
    }

    public void setGuestAccess(String guestAccess) {
        this.guestAccess = guestAccess;
    }
}
