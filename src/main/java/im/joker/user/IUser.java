package im.joker.user;

public interface IUser {


    /**
     * 获取用户名
     *
     * @return
     */
    String getUsername();


    /**
     * 获取注册时的deviceId
     *
     * @return
     */
    String getRegisterDeviceId();

    /**
     * 获取当前用户密码
     *
     * @return
     */
    String getPassword();

    /**
     * 获取userId,
     * @return
     */
    String getUserId();

}
