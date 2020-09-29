package im.joker.assist;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import im.joker.exception.JokerImException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.stream.Collectors;

import static im.joker.error.ErrorCode.INVALID_PARAM;

@Component
public class RequestProcessor {

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private Validator validator;

    /**
     * url的parameter调用这个变成bean类,支持注解校验
     *
     * @param serverRequest
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Mono<T> parameterToBean(ServerRequest serverRequest, Class<T> clazz) {
        Map<String, String> valueMap = serverRequest.queryParams().toSingleValueMap();
        T result = objectMapper.convertValue(valueMap, clazz);
        String message = validateMessage(result);
        if (StringUtils.isNoneBlank(message)) {
            throw new JokerImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, message);
        }
        return Mono.just(result);
    }

    /**
     * 提供给外部调用
     *
     * @param validTarget
     */
    public void validate(Object validTarget) {
        String message = validateMessage(validTarget);
        if (StringUtils.isNoneBlank(message)) {
            throw new JokerImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, message);
        }
    }


    private String validateMessage(Object validTarget) {
        Errors errors = new BeanPropertyBindingResult(validTarget, validTarget.getClass().getName());
        validator.validate(validTarget, errors);
        if (errors.hasErrors()) {
            return errors.getFieldErrors()
                    .stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .collect(Collectors.joining(","));
        }
        return "";
    }


}
