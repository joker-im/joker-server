package im.joker.exception;

import org.springframework.http.HttpStatus;

/**
 * 
 * @author linyurong
 *
 */
public class InvalidJsonException extends ImException {
	private static final long serialVersionUID = -7814773974650299187L;

	public InvalidJsonException(Throwable t) {
        super(ErrorCode.BAD_JSON, HttpStatus.INTERNAL_SERVER_ERROR, t.getMessage(), t);
    }

    public InvalidJsonException(String error) {
        super(ErrorCode.BAD_JSON, HttpStatus.INTERNAL_SERVER_ERROR, error);
    }

}
