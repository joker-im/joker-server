package im.joker.constants;

public class ImRedisKeys {

    /**
     * username : 设备id , value是token
     */
    public final static String DEVICE_TOKEN = "im.joker:device_token:%s:%s";

    /**
     * %s是token, value是userSession
     */
    public final static String TOKEN_USER = "im.joker:token_user:%s";


    /**
     * %s是user, value是username
     */
    public final static String USER_PRESENCE = "im.joker:user_presence:%s";

}
