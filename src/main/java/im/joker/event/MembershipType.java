package im.joker.event;

public enum MembershipType {

    Join, Invite, Leave, Ban, Knock;

    public boolean is(String type) {
        return this.name().equalsIgnoreCase(type);
    }
}
