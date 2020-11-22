package im.joker.device

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 18:20
 * @Desc:
 */
data class Device(val deviceId: String,
                  val accessToken: String,
                  val username: String,
                  val name: String?,
                  val deviceAvatar: String?,
                  val userId: String) {


}