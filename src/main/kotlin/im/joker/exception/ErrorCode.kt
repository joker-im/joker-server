package im.joker.exception

/**
 * @Author: mkCen
 * @Date: 2020/11/21
 * @Time: 16:09
 * @Desc:
 */
enum class ErrorCode(val msg:String) {
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
    M_USER_IN_USE("用户名已经存在"),
    CANNOT_LEAVE_SERVER_NOTICE_ROOM( "不允许退出通知房间"),
    RESOURCE_LIMIT_EXCEEDED( "资源超过限额"),
    UNSUPPORTED_ROOM_VERSION( "不支持该房间版本"),
    INCOMPATIBLE_ROOM_VERSION( "不兼容的房间版本"),
    WRONG_ROOM_KEYS_VERSION( "房间密钥版本错误"),
    EXPIRED_ACCOUNT( "账号过期"),
    PASSWORD_TOO_SHORT( "密码太短"),
    PASSWORD_NO_DIGIT( "密码没有包含数字"),
    PASSWORD_NO_UPPERCASE( "密码没有包含大写字母"),
    PASSWORD_NO_LOWERCASE( "密码没有包含小写字母"),
    PASSWORD_NO_SYMBOL( "密码没有包含特殊字符"),
    PASSWORD_IN_DICTIONARY( "密码容易被猜到"),
    WEAK_PASSWORD( "密码太弱"),
    INVALID_SIGNATURE( "签名错误"),
    USER_DEACTIVATED( "用户被禁用"),
    BAD_ALIAS( "别名错误"),
    M_MALFORMED_EVENT("事件格式异常");



}