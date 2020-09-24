package org.im.joker.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.im.joker.error.ErrorCode;
import org.im.joker.exception.JokerImException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

import static org.im.joker.error.ErrorCode.UNKNOWN;

@Configuration
@Order(-2)
public class JokerExceptionHandler implements ErrorWebExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(JokerExceptionHandler.class);
    @Autowired
    private ObjectMapper objectMapper;


    @SneakyThrows
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        log.error("接口路径:{}, 异常信息:{},异常栈:{} ", exchange.getRequest().getPath(), ex.getMessage(), ex.getStackTrace());
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        if (ex instanceof JokerImException) {
            ErrorCode errorCode = ((JokerImException) ex).getErrorCode();
            HttpStatus httpStatus = ((JokerImException) ex).getHttpStatus();
            exchange.getResponse().setStatusCode(httpStatus);

            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(Map.of("error_code", errorCode.name(), "msg", errorCode.getMsg())))
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(e -> exchange.getResponse().bufferFactory().wrap(e))
                    .flatMap(e -> exchange.getResponse().writeWith(Mono.just(e)));

        } else {
            exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return Mono.fromCallable(() -> objectMapper.writeValueAsBytes(Map.of("error_code", UNKNOWN.name(), "msg", UNKNOWN.getMsg())))
                    .subscribeOn(Schedulers.boundedElastic())
                    .map(e -> exchange.getResponse().bufferFactory().wrap(e))
                    .flatMap(e -> exchange.getResponse().writeWith(Mono.just(e)));
        }

    }
}
