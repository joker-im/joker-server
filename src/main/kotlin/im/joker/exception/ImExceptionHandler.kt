package im.joker.exception

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.core.publisher.MonoSink
import java.util.Map

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:22
 * @Desc:
 */
@Configuration
@Order(-2)
class ImExceptionHandler : ErrorWebExceptionHandler {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    private val log: Logger = LoggerFactory.getLogger(ImExceptionHandler::class.java)


    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        log.error("接口路径:{}, 异常信息:{},异常栈:{} ", exchange.request.path, ex.message, ex.stackTrace)
        exchange.response.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        return if (ex is ImException) {
            val errorCode: ErrorCode = ex.errorCode
            val httpStatus: HttpStatus = ex.httpStatus
            exchange.response.statusCode = httpStatus
            val msg = StringUtils.defaultIfBlank(ex.customMsg, errorCode.msg)
            Mono.create { monoSink: MonoSink<DataBuffer> ->
                var jsonBytes = ByteArray(0)
                try {
                    jsonBytes = objectMapper.writeValueAsBytes(Map.of("error_code", errorCode.name, "msg", msg))
                } catch (e: Exception) {
                    log.error("json转换异常", e)
                }
                val dataBuffer = exchange.response.bufferFactory().wrap(jsonBytes)
                monoSink.success(dataBuffer)
            }.flatMap { e: DataBuffer -> exchange.response.writeWith(Mono.just(e)) }
        } else {
            exchange.response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR
            Mono.create { monoSink: MonoSink<DataBuffer> ->
                var jsonBytes = ByteArray(0)
                try {
                    jsonBytes = objectMapper.writeValueAsBytes(Map.of("error_code", ErrorCode.UNKNOWN.name, "msg", ErrorCode.UNKNOWN.msg))
                } catch (e: Exception) {
                    log.error("json转换异常", e)
                }
                val dataBuffer = exchange.response.bufferFactory().wrap(jsonBytes)
                monoSink.success(dataBuffer)
            }.flatMap { e: DataBuffer -> exchange.response.writeWith(Mono.just(e)) }
        }
    }
}