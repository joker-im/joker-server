package im.joker.error;

public enum ErrorCode {

    UNRECOGNIZED( "无法识别请求"),
    UNAUTHORIZED( "权限不足"),
    FORBIDDEN( "拒绝访问,可能未登录"),
    BAD_JSON( "请求报文不满足要求,缺失必要信息"),
    NOT_JSON( "请求报文格式解析有误"),
    USER_IN_USE( "该用户已经被创建"),
    ROOM_IN_USE( "该房间已经被创建"),
    BAD_PAGINATION( "页码参数不正确"),
    BAD_STATE( "无法完成状态变更"),
    UNKNOWN( "未知异常"),
    NOT_FOUND( "此请求无法找到对应资源"),
    MISSING_TOKEN( "token缺失"),
    UNKNOWN_TOKEN( "token识别错误"),
    GUEST_ACCESS_FORBIDDEN( "不允许访客访问"),
    LIMIT_EXCEEDED( "系统资源不足"),
    CAPTCHA_NEEDED( "密码缺失"),
    CAPTCHA_INVALID( "密码无效"),
    MISSING_PARAM( "缺失请求参数"),
    INVALID_PARAM( "无效的请求参数"),
    TOO_LARGE( "请求过大"),
    INVALID_USERNAME( "无效的用户名"),
    CANNOT_LEAVE_SERVER_NOTICE_ROOM( ""),
    RESOURCE_LIMIT_EXCEEDED( ""),
    UNSUPPORTED_ROOM_VERSION( ""),
    INCOMPATIBLE_ROOM_VERSION( ""),
    WRONG_ROOM_KEYS_VERSION( ""),
    EXPIRED_ACCOUNT( ""),
    PASSWORD_TOO_SHORT( ""),
    PASSWORD_NO_DIGIT( ""),
    PASSWORD_NO_UPPERCASE( ""),
    PASSWORD_NO_LOWERCASE( ""),
    PASSWORD_NO_SYMBOL( ""),
    PASSWORD_IN_DICTIONARY( ""),
    WEAK_PASSWORD( ""),
    INVALID_SIGNATURE( ""),
    USER_DEACTIVATED( ""),
    BAD_ALIAS( "");

    private String msg;

    ErrorCode(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    

}
