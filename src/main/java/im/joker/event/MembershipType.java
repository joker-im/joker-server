package im.joker.event;

/**
 * @author zhuangbiwang
 */

public enum MembershipType {

    Join("join"), Invite("invite"), Leave("leave"), Ban("ban"), Knock("knock");
    private final String id;

    MembershipType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean is(String type) {
        return id.equalsIgnoreCase(type);
    }

    public static MembershipType find(String type) {
        for (MembershipType value : MembershipType.values()) {
            if (value.is(type)) {
                return value;
            }
        }
        return null;
    }
}
