package im.joker.config.filter;

import im.joker.device.IDevice;
import im.joker.exception.ErrorCode;
import im.joker.exception.ImException;
import im.joker.helper.GlobalStateHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.reactive.ServerWebExchangeContextFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.List;

import static im.joker.exception.ErrorCode.FORBIDDEN;

@Component
@Slf4j
public class AuthFilter implements WebFilter {

    @Autowired
    private GlobalStateHolder globalStateHolder;

    private final List<Tuple2<HttpMethod, String>> withoutAuthUrls = List.of(
            Tuples.of(HttpMethod.POST, "/_matrix/client/r0/register"),
            Tuples.of(HttpMethod.GET, "/_matrix/client/r0/login"),
            Tuples.of(HttpMethod.GET, "/_matrix/client/versions"),
            Tuples.of(HttpMethod.POST, "/_matrix/client/r0/login")
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String url = request.getPath().value();
        HttpMethod method = request.getMethod();
        boolean exists = withoutAuthUrls.stream().anyMatch(e -> e.getT1().equals(method) && e.getT2().equals(url));
        if (HttpMethod.OPTIONS.equals(method) || exists) {
            return chain.filter(exchange);
        }
        List<String> headers = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
        String token;
        if (CollectionUtils.isEmpty(headers)) {
            throw new ImException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED);
        }
        token = StringUtils.substringAfter(headers.get(0), "Bearer ");
        if (StringUtils.equals(token, "undefined")) {
            throw new ImException(ErrorCode.UNKNOWN_TOKEN, HttpStatus.FORBIDDEN);
        }
        Mono<IDevice> device = globalStateHolder.getDeviceManager().find(token);
        return device.switchIfEmpty(Mono.error(new ImException(FORBIDDEN, HttpStatus.FORBIDDEN)))
                .flatMap(e -> chain.filter(exchange).subscriberContext(context -> context.put("device", e)));


    }
}
