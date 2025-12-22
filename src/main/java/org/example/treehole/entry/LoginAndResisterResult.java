package org.example.treehole.entry;

import lombok.Data;
import org.example.treehole.Constant;
import org.example.treehole.enums.loginAndResisterStatus;

/**
 * @author pluchon
 * @create 2025-12-21-10:33
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
//各种情况都可以使用
public class LoginAndResisterResult<T> {
    private loginAndResisterStatus status;
    private String errorMessage;
    private T data;

    // 增加一个方法，确保前端能拿到数字类型的状态码
    public Integer getStatusCode() {
        return status != null ? status.getCode() : null;
    }

    //登录成功
    public static <T>LoginAndResisterResult success(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.SUCCESS);
        result.setErrorMessage("成功登录，等待跳转......");
        return result;
    }

    //纯数字或者是以数字开头的非法字符或者是负数或者带有小数点
    public static LoginAndResisterResult digit(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.ILLEGALCHARACTERFORDIGIT);
        result.setErrorMessage("非法的数字字符输入");
        return result;
    }

    //超过规定长度
    public static LoginAndResisterResult overLength(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.OVERLENGTH);
        result.setErrorMessage("账号或密码超过规定长度");
        return result;
    }

    //不足规定长度
    public static LoginAndResisterResult inSufficientLength(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.INSUFFICIENTCHARACTER);
        result.setErrorMessage("账号或密码不足规定长度");
        return result;
    }

    //验证码无效或未获取
    public static LoginAndResisterResult captchaIllegal(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.CAPTCHAILLEGAL);
        result.setErrorMessage("验证码无效或未获取");
        return result;
    }

    //验证码已过期
    public static LoginAndResisterResult captchaTimeOut(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.CAPTCHATIMEOUT);
        result.setErrorMessage("验证码已过期");
        return result;
    }

    //验证码错误
    public static LoginAndResisterResult captchaError(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.CAPTCHAERROR);
        result.setErrorMessage("验证码错误");
        return result;
    }

    //用户已存在
    public static LoginAndResisterResult userExists(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.USEREXISTS);
        result.setErrorMessage("用户已存在");
        return result;
    }

    //昵称错误
    public static LoginAndResisterResult nickNameError(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.NICKNAMEERROR);
        result.setErrorMessage("昵称包含非法字符或长度不符合要求");
        return result;
    }

    //未登录
    public static LoginAndResisterResult notLogin(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.NOT_LOGIN);
        result.setErrorMessage("请先登录");
        return result;
    }

    //非法字符
    public static LoginAndResisterResult illegalCharacter(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.ILLEGAL_CHARACTER);
        result.setErrorMessage("包含非法字符（如空格、特殊符号等）");
        return result;
    }

    //不能给自己发送私信
    public static LoginAndResisterResult sendMessageByMyself(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.SEND_MESSAGE_MYSELF);
        result.setErrorMessage("不能自己给自己发私信啊笨蛋！！");
        return result;
    }

    //内容过长
    public static LoginAndResisterResult textOver(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.TEXT_OVER);
        result.setErrorMessage("你写那么多东西干嘛");
        return result;
    }

    //内容过短
    public static LoginAndResisterResult textInSufficient(){
        LoginAndResisterResult result = new LoginAndResisterResult();
        result.setStatus(loginAndResisterStatus.TEXT_INSUFFICIENT);
        result.setErrorMessage("你写这么少东西好意思发吗？");
        return result;
    }
}
