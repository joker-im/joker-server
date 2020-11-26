package im.joker.event;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 12:06
 * @Desc:
 */
public enum RoomJoinRuleType {
    /**
     * 目前只实现前2种, 一种是房间是随时都能被加入, 一种是需要在房间里的人去邀请想加入此房间的人
     */
    Public("public"), Invite("invite"), Private("private"), Knock("knock"),
    ;

    private String id;

    RoomJoinRuleType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean is(String type) {
        return this.name().equalsIgnoreCase(type);
    }
}
