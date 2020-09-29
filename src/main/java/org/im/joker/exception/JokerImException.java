package org.im.joker.exception;

import org.im.joker.error.ErrorCode;
import org.springframework.http.HttpStatus;

public class JokerImException extends RuntimeException {

    private final ErrorCode errorCode;

    private final HttpStatus httpStatus;

    private String customMsg;


    /**
     * 不需要返回自定义错误消息的时候, 代码里面抛出这个错误
     *
     * @param errorCode
     * @param httpStatus
     */
    public JokerImException(ErrorCode errorCode, HttpStatus httpStatus) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    /**
     * 当需要自定义返回错误消息的时候,代码里面抛出这个错误
     *
     * @param errorCode
     * @param httpStatus
     * @param customMsg
     */
    public JokerImException(ErrorCode errorCode, HttpStatus httpStatus, String customMsg) {
        super(errorCode.getMsg());
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.customMsg = customMsg;
    }


    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCustomMsg() {
        return customMsg;
    }
}
