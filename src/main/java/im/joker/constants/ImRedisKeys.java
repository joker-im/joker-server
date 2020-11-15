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

    public final static String TOKEN_USER_HASH_KEY_USERNAME = "username";

    public final static String TOKEN_USER_HASH_KEY_DEVICE_ID = "device_id";

    public final static String TOKEN_USER_HASH_HASH_KEY_USER_ID = "user_id";

    public final static String TOKEN_USER_HASH_HASH_KEY_DEVICE_NAME = "device_name";


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
     * 活动房间最新的事件集合
     * %s是room_id
     */
    public final static String ACTIVE_ROOM_LATEST_EVENTS = "im.joker:active_room_latest_events:%s";


    /**
     * 房间的订阅者, 是一个set集合,%s是roomId,value是deviceIds
     *
     */
    public final static String ROOM_SUBSCRIBERS_OF_DEVICE = "im.joker:room_subscribers_of_device:%s";

}
