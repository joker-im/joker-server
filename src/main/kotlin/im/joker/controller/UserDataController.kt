package im.joker.controller

import im.joker.api.vo.user.UserDirectoryRequest
import im.joker.api.vo.user.UserDirectoryResponse
import im.joker.api.vo.user.UserProfileResponse
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.handler.MediaHandler
import im.joker.handler.UserHandler
import im.joker.helper.RequestProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

/**
 * @Author: mkCen
 * @Date: 2020/11/26
 * @Time: 21:51
 * @Desc:
 */
@RestController
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserDataController : BaseController() {

    @Autowired
    private lateinit var userHandler: UserHandler

    @Autowired
    private lateinit var mediaHandler: MediaHandler


    @Autowired
    private lateinit var requestProcessor: RequestProcessor

    @GetMapping("/profile/{userId}")
    suspend fun retrieveProfile(@PathVariable userId: String): UserProfileResponse {
        val user = userHandler.findUser(userId)
        user ?: throw ImException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "用户不存在")
        return UserProfileResponse().apply {
            displayName = user.displayName
            avatarUrl = user.avatar
        }
    }


    @PostMapping("/user_directory/search")
    suspend fun searchUser(@RequestBody userDirectoryRequest: UserDirectoryRequest): UserDirectoryResponse {
        requestProcessor.validate(userDirectoryRequest)
        val users = userHandler.findUsersByTerm(userDirectoryRequest.searchTerm, userDirectoryRequest.limit ?: 10)
        return UserDirectoryResponse().apply {
            userDirectoryRequest.limit?.let {
                this.limited = true
            }
            this.results = users.map {
                return@map UserDirectoryResponse.UserInfo().apply {
                    avatarUrl = mediaHandler.toMediaUrl(it.avatar)
                    displayName = it.displayName
                    userId = it.userId
                }
            }
        }
    }
}