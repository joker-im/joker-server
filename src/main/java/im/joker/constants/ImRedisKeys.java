package im.joker.constants;

public class ImRedisKeys {

    /**
     * token键,用来看哪个用户申请了token
     * username : 设备id , value是token
     */
    public final static String DEVICE_TOKEN = "im.joker:device_token:%s:%s";

    /**
     * token键,用来看token是哪个用户的
     * %s是token, value是userSession
     */
    public final static String TOKEN_USER = "im.joker:token_user:%s";


    /**
     * 状态键,用来定位当前用户的登录状态
     * %s是user, value是username
     */
    public final static String USER_PRESENCE = "im.joker:user_presence:%s";


    /**
     * stream的自增序列
     */
    public final static String EVENT_STREAM_SEQUENCE = "im.joker:event_stream_sequence";

}
