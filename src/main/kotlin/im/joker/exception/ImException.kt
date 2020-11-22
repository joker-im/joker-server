package im.joker.exception

import org.springframework.http.HttpStatus

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:15
 * @Desc:
 */
class ImException : RuntimeException {


    var errorCode: ErrorCode

    var httpStatus: HttpStatus

    var customMsg: String? = null

    constructor(errorCode: ErrorCode, httpStatus: HttpStatus) : super(errorCode.msg) {
        this.errorCode = errorCode
        this.httpStatus = httpStatus
    }

    constructor(errorCode: ErrorCode, httpStatus: HttpStatus, customMsg: String) : super(customMsg) {
        this.errorCode = errorCode
        this.httpStatus = httpStatus
        this.customMsg = customMsg
    }


}