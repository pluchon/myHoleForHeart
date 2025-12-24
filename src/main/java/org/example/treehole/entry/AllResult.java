package org.example.treehole.entry;

import lombok.Data;
import org.example.treehole.enums.loginAndResisterStatus;

/**
 * @author pluchon
 * @create 2025-12-21-10:33
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
//各种情况都可以使用
public class AllResult<T> {
    private loginAndResisterStatus status;
    private String errorMessage;
    private T data;

    // 增加一个方法，确保前端能拿到数字类型的状态码
    public Integer getStatusCode() {
        return status != null ? status.getCode() : null;
    }

    //登录成功
    public static <T> AllResult success(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.SUCCESS);
        result.setErrorMessage("成功登录，等待跳转......");
        return result;
    }

    //纯数字或者是以数字开头的非法字符或者是负数或者带有小数点
    public static AllResult digit(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.ILLEGALCHARACTERFORDIGIT);
        result.setErrorMessage("非法的数字字符输入");
        return result;
    }

    //超过规定长度
    public static AllResult overLength(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.OVERLENGTH);
        result.setErrorMessage("账号或密码超过规定长度");
        return result;
    }

    //不足规定长度
    public static AllResult inSufficientLength(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.INSUFFICIENTCHARACTER);
        result.setErrorMessage("账号或密码不足规定长度");
        return result;
    }

    //验证码无效或未获取
    public static AllResult captchaIllegal(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.CAPTCHAILLEGAL);
        result.setErrorMessage("验证码无效或未获取");
        return result;
    }

    //验证码已过期
    public static AllResult captchaTimeOut(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.CAPTCHATIMEOUT);
        result.setErrorMessage("验证码已过期");
        return result;
    }

    //验证码错误
    public static AllResult captchaError(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.CAPTCHAERROR);
        result.setErrorMessage("验证码错误");
        return result;
    }

    //用户已存在
    public static AllResult userExists(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.USEREXISTS);
        result.setErrorMessage("用户已存在");
        return result;
    }

    //昵称错误
    public static AllResult nickNameError(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.NICKNAMEERROR);
        result.setErrorMessage("昵称包含非法字符或长度不符合要求");
        return result;
    }

    //未登录
    public static AllResult notLogin(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.NOT_LOGIN);
        result.setErrorMessage("请先登录");
        return result;
    }

    //不能关注自己
    public static AllResult followMyself(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.FOLLOW_MYSELF);
        result.setErrorMessage("不能关注自己");
        return result;
    }

    //未关注该用户
    public static AllResult notFollowed(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.NOT_FOLLOWED);
        result.setErrorMessage("未关注该用户");
        return result;
    }

    //关注操作失败
    public static AllResult followOpFailed(String msg){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.FOLLOW_OP_FAILED);
        result.setErrorMessage(msg != null ? msg : "关注操作失败");
        return result;
    }

    //非法字符
    public static AllResult illegalCharacter(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.ILLEGAL_CHARACTER);
        result.setErrorMessage("包含非法字符（如空格、特殊符号等）");
        return result;
    }

    //不能给自己发送私信
    public static AllResult sendMessageByMyself(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.SEND_MESSAGE_MYSELF);
        result.setErrorMessage("不能自己给自己发私信啊笨蛋！！");
        return result;
    }

    //内容过长
    public static AllResult textOver(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.TEXT_OVER);
        result.setErrorMessage("你写那么多东西干嘛");
        return result;
    }

    //内容过短
    public static AllResult textInSufficient(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.TEXT_INSUFFICIENT);
        result.setErrorMessage("你写这么少东西好意思发吗？");
        return result;
    }

    //时空胶囊埋藏失败
    public static AllResult timeCapsuleError(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.TIME_CAPSULE);
        result.setErrorMessage("埋藏失败，请检查时间或网络连接状态");
        return result;
    }

    //匿名树洞发布失败
    public static AllResult anonymousPublishFailed(){
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.ANONYMOUS_PUBLISH_FAILED);
        result.setErrorMessage("投递失败，请稍后再试");
        return result;
    }

    //今日已签到
    public static AllResult checkInAlready() {
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.CHECK_IN_ALREADY);
        result.setErrorMessage("今天已经签到过了哦~");
        return result;
    }

    public static <T> AllResult<T> successWithData(T data) {
        AllResult<T> result = new AllResult<>();
        result.setStatus(loginAndResisterStatus.SUCCESS);
        result.setErrorMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static AllResult fail() {
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.AI_ERROR);
        result.setErrorMessage("操作失败");
        return result;
    }

    public static AllResult aiError(String msg) {
        AllResult result = new AllResult();
        result.setStatus(loginAndResisterStatus.AI_ERROR);
        result.setErrorMessage(msg != null ? msg : "AI服务暂时不可用");
        return result;
    }
}
