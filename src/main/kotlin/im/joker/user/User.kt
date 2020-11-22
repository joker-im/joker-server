package im.joker.user

import im.joker.helper.GlobalStateHolder
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 17:37
 * @Desc:
 */
@Document
class User {


    @Id
    lateinit var id: String

    lateinit var userId: String

    lateinit var username: String

    @Transient
    val globalStateHolder: GlobalStateHolder? = null

    var displayName: String? = null

    var avatarUrl: String? = null

    lateinit var createTime: LocalDateTime

    lateinit var registerDeviceId: String

    lateinit var password: String


}