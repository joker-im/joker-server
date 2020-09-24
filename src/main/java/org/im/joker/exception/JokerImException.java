package org.im.joker.exception;

import org.im.joker.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class JokerImException extends RuntimeException {

    private final ErrorCode errorCode;

    private final HttpStatus httpStatus;

    public JokerImException( ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
