package im.joker.constants;

public class ImRedisKeys {

    /**
     * token键,用来看哪个用户申请了token
     * %s  : username
     * hashKey : device_id , token
     */
    public final static String USER_DEVICES_TOKENS_HASH = "im.joker:devices_tokens_hash:%s";

    /**
     * token键,用来看token是哪个用户的
     * %s是token, value是userSession (deviceId,username)
     */
    public final static String TOKEN_USER_HASH = "im.joker:token_user:%s";


    /**
     * 状态键,用来定位当前用户的登录状态
     * %s是user, value是 ["online", "offline", "unavailable"] 其中之一
     */
    public final static String USER_PRESENCE = "im.joker:user_presence:%s";


    /**
     * stream的自增序列
     */
    public final static String EVENT_STREAM_SEQUENCE = "im.joker:event_stream_sequence";


    /**
     * 设备关心的房间号,是一个set集合, 在inject和sync的时候,会维护这个集合,每个设备都有这个key
     * %s是device_id
     */
    public final static String DEVICE_CARE_ROOM_SET = "im.joker:care_room_set:%s";

}
