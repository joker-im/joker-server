package im.joker.router

import im.joker.api.vo.account.LoginFlowResponse
import im.joker.api.vo.account.LoginRequest
import im.joker.api.vo.account.LoginResponse
import im.joker.api.vo.account.RegisterRequest
import im.joker.config.AuthFilter
import im.joker.device.Device
import im.joker.handler.UserHandler
import kotlinx.coroutines.reactive.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:48
 * @Desc:
 */
@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class AccountRouter {

    @Autowired
    lateinit var userHandler: UserHandler


    @PostMapping("/register")
    suspend fun register(@RequestBody registerRequest: RegisterRequest): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(userHandler.register(registerRequest))
        } catch (e: Exception) {
            val json = """{"session":"FsfiufEOEvnjQXJRBSyTTdNr","flows":[{"stages":["m.login.recaptcha","m.login.terms","m.login.dummy"]},{"stages":["m.login.recaptcha","m.login.terms","m.login.email.identity"]}],"params":{"m.login.recaptcha":{"public_key":"6LcgI54UAAAAABGdGmruw6DdOocFpYVdjYBRe4zb"},"m.login.terms":{"policies":{"privacy_policy":{"version":"1.0","en":{"name":"Terms and Conditions","url":"https://matrix-client.matrix.org/_matrix/consent?v=1.0"}}}}}}"""
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
        }
    }

    @PostMapping("/login")
    suspend fun login(@RequestBody request: LoginRequest): LoginResponse {
        return userHandler.login(request)
    }

    @GetMapping("/login")
    suspend fun queryLoginFlows(): LoginFlowResponse {
        return LoginFlowResponse().apply {
            this.flows = listOf(LoginFlowResponse.LoginFlow().apply { this.type = "m.login.password" })
        }
    }

    @PostMapping("/logout")
    suspend fun logout() {
        val loginDevice = Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingleOrNull()
        userHandler.logout(loginDevice)
    }

    @PostMapping("/logout/all")
    suspend fun logoutAll() {
        val loginDevice = Mono.deferContextual<Device> { Mono.just(it[AuthFilter.LOGIN_DEVICE]) }.awaitSingleOrNull()
        userHandler.logoutAll(loginDevice)
    }
}