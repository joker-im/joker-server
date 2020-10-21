package im.joker.exception;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.Map;
import java.util.function.Consumer;

@Configuration
@Order(-2)
@Slf4j
public class ImExceptionHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("接口路径:{}, 异常信息:{},异常栈:{} ", exchange.getRequest().getPath(), ex.getMessage(), ex.getStackTrace());
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (ex instanceof ImException) {
            ErrorCode errorCode = ((ImException) ex).getErrorCode();
            HttpStatus httpStatus = ((ImException) ex).getHttpStatus();
            exchange.getResponse().setStatusCode(httpStatus);
            String msg = StringUtils.defaultIfBlank(((ImException) ex).getCustomMsg(), errorCode.getMsg());
            return Mono.create((Consumer<MonoSink<DataBuffer>>) monoSink -> {
                byte[] jsonBytes = new byte[0];
                try {
                    jsonBytes = objectMapper.writeValueAsBytes(Map.of("error_code", errorCode.name(), "msg", msg));
                } catch (Exception e) {
                    log.error("json转换异常", e);
                }
                DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(jsonBytes);
                monoSink.success(dataBuffer);
            }).flatMap(e -> exchange.getResponse().writeWith(Mono.just(e)));

        } else {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return Mono.create((Consumer<MonoSink<DataBuffer>>) monoSink -> {
                byte[] jsonBytes = new byte[0];
                try {
                    jsonBytes = objectMapper.writeValueAsBytes(Map.of("error_code", ErrorCode.UNKNOWN.name(), "msg", ErrorCode.UNKNOWN.getMsg()));
                } catch (Exception e) {
                    log.error("json转换异常", e);
                }
                DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(jsonBytes);
                monoSink.success(dataBuffer);
            }).flatMap(e -> exchange.getResponse().writeWith(Mono.just(e)));
        }

    }
}
