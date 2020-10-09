package im.joker.exception;

import org.springframework.http.HttpStatus;

/**
 * @author linyurong
 * @date 2020/9/30 14:16
 */
public class MalformedEventException extends ImException {

    public MalformedEventException(String errorMsg) {
        super(ErrorCode.M_MALFORMED_EVENT, HttpStatus.INTERNAL_SERVER_ERROR, errorMsg);
    }
}
