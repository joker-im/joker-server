package im.joker.device;

public interface IDevice {

    /**
     * 获取设备id
     *
     * @return
     */
    String getDeviceId();

    /**
     * 获取设备对应的token
     *
     * @return
     */
    String getAccessToken();

    /**
     * 获取该设备的用户名
     *
     * @return
     */
    String getUsername();

    /**
     * 获取该设备名称
     *
     * @return
     */
    String getName();

    /**
     * 获取用户id
     *
     * @return
     */
    String getUserId();
}
