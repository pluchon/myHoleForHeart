package org.example.treehole.enums;

/**
 * @author pluchon
 * @create 2025-12-21-10:25
 * 作者代码水平一般，难免难看，请见谅
 */
public enum loginAndResisterStatus {
    //使用枚举类枚举注册或登录状态
    SUCCESS(100),//登录成功
    ILLEGALCHARACTERFORDIGIT(-1),//纯数字或者是以数字开头或者是小数点的非法字符
    OVERLENGTH(-2),//超过规定长度
    INSUFFICIENTCHARACTER(-3),//不足规定长度
    CAPTCHAILLEGAL(-4),//验证码无效或未获取
    CAPTCHATIMEOUT(-5),//验证码过期
    CAPTCHAERROR(-6),//验证码错误
    USEREXISTS(-7),//用户已存在
    NICKNAMEERROR(-8),//昵称错误
    PASSWORDERROR(-9),//密码错误
    NOT_LOGIN(-10),//未登录
    ILLEGAL_CHARACTER(-11),//非法字符
    SEND_MESSAGE_MYSELF(-12),//自己给自己发私信
    TEXT_OVER(-13),//内容过长
    TEXT_INSUFFICIENT(-14);//内容过短

    private Integer code;

    loginAndResisterStatus(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
