package im.joker.config

import im.joker.device.Device
import im.joker.exception.ErrorCode
import im.joker.exception.ImException
import im.joker.helper.GlobalStateHolder
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.util.AntPathMatcher
import org.springframework.util.CollectionUtils
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import reactor.util.context.Context
import reactor.util.function.Tuple2
import reactor.util.function.Tuples

/**
 * @Author: mkCen
 * @Date: 2020/11/22
 * @Time: 0:54
 * @Desc:
 */
@Component
class AuthFilter : WebFilter {
    private val log: Logger = LoggerFactory.getLogger(AuthFilter::class.java)

    @Autowired
    lateinit var globalStateHolder: GlobalStateHolder

    companion object {
        const val LOGIN_DEVICE = "login_device"
    }

    private val withoutAuthUrls = listOf(
            Tuples.of(HttpMethod.POST, "/_matrix/client/r0/register"),
            Tuples.of(HttpMethod.GET, "/_matrix/client/r0/login"),
            Tuples.of(HttpMethod.GET, "/_matrix/client/versions"),
            Tuples.of(HttpMethod.POST, "/_matrix/client/r0/login"),
            Tuples.of(HttpMethod.GET, "/_matrix/client/versions/test"),
            Tuples.of(HttpMethod.GET, "/favicon.ico"),
            Tuples.of(HttpMethod.GET, "/_matrix/media/r0/download/**"),
            Tuples.of(HttpMethod.GET, "/_matrix/media/r0/config")
    )

    private val antMatcher = AntPathMatcher()

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val url = request.path.value()
        val method = request.method
        val exists = withoutAuthUrls.stream().anyMatch { e: Tuple2<HttpMethod, String> -> e.t1 == method && antMatcher.match(e.t2, url) }
        if (HttpMethod.OPTIONS == method || exists) {
            return chain.filter(exchange)
        }
        val headers = request.headers[HttpHeaders.AUTHORIZATION]
        var token = exchange.getAttribute<String>("access_token")
        headers?.let {
            token = it[0].substringAfter("Bearer ")
        }
        if (token.isNullOrBlank()) {
            throw ImException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED)
        }

        if (StringUtils.equals(token, "undefined")) {
            throw ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN)
        }
        val device: Mono<Device> = globalStateHolder.deviceManager.find(token!!)
        return device.switchIfEmpty(Mono.error(ImException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN)))
                .flatMap { e: Device -> chain.filter(exchange).contextWrite { context: Context -> context.put(LOGIN_DEVICE, e) } }

    }


}