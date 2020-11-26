package im.joker.router

import im.joker.api.vo.user.UserProfileResponse
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.handler.UserHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @Author: mkCen
 * @Date: 2020/11/26
 * @Time: 21:51
 * @Desc:
 */
@RestController
@RequestMapping(path = ["/_matrix/client/r0/profile"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserDataRouter : BaseRouter() {

    @Autowired
    private lateinit var userHandler: UserHandler

    @GetMapping("/{userId}")
    suspend fun retrieveProfile(@PathVariable userId: String): UserProfileResponse {
        val user = userHandler.findUser(userId)
        user ?: throw ImException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "用户不存在")
        return UserProfileResponse().apply {
            displayName = user.displayName
            avatarUrl = user.avatar
        }
    }
}