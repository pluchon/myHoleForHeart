package org.example.treehole;

import java.util.regex.Pattern;

/**
 * @author pluchon
 * @create 2025-12-20-16:06
 * 作者代码水平一般，难免难看，请见谅
 */
public class Constant {
    public static final String USER_NAME = "USER_NAME";
    public static final String USER_ID = "USER_ID";
    public static final String PASSWORD = "PASSWORD";
    public static final Integer PAGE_SIZE = 15;
    
    // 验证码Session Key
    public static final String CAPTCHA_SESSION_KEY = "captcha_code";
    public static final String CAPTCHA_TIME_KEY = "captcha_time";
    public static final String CAPTCHA_TYPE_KEY = "captcha_type"; //验证码类型
    public static final String GALGAME_TARGET_COLOR_KEY = "galgame_target_color"; //GalGame目标颜色
    public static final String GALGAME_CORRECT_INDEX_KEY = "galgame_correct_index"; //GalGame正确索引
    // 验证码过期时间 2分钟
    public static final long CAPTCHA_EXPIRATION_TIME = 2 * 60 * 1000;

    //账号密码昵称长度限定
    public static final Integer MIN_LENGTH = 6;
    public static final Integer MAX_LENGTH = 20;

    //规定私信内容长度
    public static final Integer MESSAGE_MIN_LENGTH = 5;
    public static final Integer MESSAGE_MAX_LENGTH = 200;

    //树洞内容长度
    public static final Integer HOLE_TEXT_MIN_LENGTH = 10;
    public static final Integer HOLE_TEXT_MAX_LENGTH = 200;
}
