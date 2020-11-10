package im.joker.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import im.joker.exception.ImException;
import org.apache.commons.lang3.StringUtils;
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

import static im.joker.exception.ErrorCode.INVALID_PARAM;

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
    public <T> Mono<T> parameterToMono(ServerRequest serverRequest, Class<T> clazz) {
        T result = convert(serverRequest, clazz);
        String message = validateMessage(result);
        if (StringUtils.isNoneBlank(message)) {
            throw new ImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, message);
        }
        return Mono.just(result);
    }

    public <T> T parameterToBean(ServerRequest serverRequest, Class<T> clazz) {
        T result = convert(serverRequest, clazz);
        String message = validateMessage(result);
        if (StringUtils.isNoneBlank(message)) {
            throw new ImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, message);
        }
        return result;
    }

    public <T> T convert(Map<String, Object> param, Class<T> clazz) {
        return objectMapper.convertValue(param, clazz);
    }

    private <T> T convert(ServerRequest serverRequest, Class<T> clazz) {
        Map<String, String> valueMap = serverRequest.queryParams().toSingleValueMap();
        return objectMapper.convertValue(valueMap, clazz);
    }

    /**
     * 提供给外部调用
     *
     * @param validTarget
     */
    public void validate(Object validTarget) {
        String message = validateMessage(validTarget);
        if (StringUtils.isNoneBlank(message)) {
            throw new ImException(INVALID_PARAM, HttpStatus.BAD_REQUEST, message);
        }
    }


    public String validateMessage(Object validTarget) {
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
