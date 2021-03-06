package im.joker.helper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import im.joker.exception.ImException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final Logger log = LoggerFactory.getLogger(RequestProcessor.class);

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


    public <T> T toBean(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new ImException(INVALID_PARAM, HttpStatus.INTERNAL_SERVER_ERROR, "服务端转换错误");

        }
    }

    public ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
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

    public String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("转换成json报错", e);
        }
        return "";
    }

}
