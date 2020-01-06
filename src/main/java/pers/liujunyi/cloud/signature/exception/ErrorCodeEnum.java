package pers.liujunyi.cloud.signature.exception;

/***
 * 异常代码 值
 * @author Administrator
 */

public enum ErrorCodeEnum {

    SUCCESS("success.", 200),
    FAIL("fail.", 202),
    PARAMS("请求参数有误.", 400),
    ERROR("服务器处理数据遇到错误,请联系管理员.", 500),
    AUTHORITY("无访问权限,请联系管理员.", 403 ),
    LOGIN_WITHOUT("你尚未登录或者已超时,请重新登录.", 504),
    LOGIN_INCORRECT("登录账户或者密码错误.", 300),
    USER_LOCK("你登陆的用户已被停用,请联系客服.", 301),
    TOKEN_INVALID("无效的用户token.", 530),
    SIGN_INVALID("非法请求：数字签名错误.", 401),
    SIGN_TIME_OUT("非法请求：请求已过期.", 408),
    DATA_LOCK("数据被锁,已被他人修改,请稍候再试!", 409),
    NO_TOKEN("要访问此资源，需要身份验证,缺少token参数,必须在headers中Authorization传递token值.", 401);

    private String message ;
    private Integer code ;

    ErrorCodeEnum(String message, Integer code) {
        this.message = message;
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
