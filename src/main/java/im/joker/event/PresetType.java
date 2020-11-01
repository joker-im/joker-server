package im.joker.event;

/**
 * @Author: mkCen
 * @Date: 2020/10/31
 * @Time: 12:12
 * @Desc:
 */
public enum PresetType {

    Private_chat, Trusted_private_chat, Public_chat,
    ;

    public static PresetType find(String type) {
        for (PresetType value : PresetType.values()) {
            if (value.is(type)) {
                return value;
            }
        }
        return null;
    }

    public boolean is(String type) {
        return this.name().equalsIgnoreCase(type);
    }

}
