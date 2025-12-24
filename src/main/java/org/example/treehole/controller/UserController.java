package org.example.treehole.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ICaptcha;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.AllExceptionResult;
import org.example.treehole.entry.TopUserDTO;
import org.example.treehole.entry.User;
import org.example.treehole.enums.loginAndResisterStatus;
import org.example.treehole.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-16:06
 * 作者代码水平一般，难免难看，请见谅
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    // 生成验证码
    @GetMapping("/captcha")
    public void getCaptcha(@RequestParam(defaultValue = "line") String type, HttpSession session, HttpServletResponse response) throws IOException {
        ICaptcha captcha;
        switch (type) {
            case "circle":
                // 圆圈干扰验证码
                captcha = CaptchaUtil.createCircleCaptcha(120, 40, 4, 20);
                break;
            case "shear":
                // 扭曲干扰验证码
                captcha = CaptchaUtil.createShearCaptcha(120, 40, 4, 4);
                break;
            case "gif":
                // GIF验证码
                captcha = CaptchaUtil.createGifCaptcha(120, 40, 4);
                break;
            case "line":
            default:
                // 线段干扰验证码
                captcha = CaptchaUtil.createLineCaptcha(120, 40, 4, 20);
                break;
        }
        
        // 获取验证码
        String code = captcha.getCode();
        
        // 存入Session
        session.setAttribute(Constant.CAPTCHA_SESSION_KEY, code);
        session.setAttribute(Constant.CAPTCHA_TIME_KEY, System.currentTimeMillis());
        // 清除 GalGame 相关的 session
        session.removeAttribute(Constant.CAPTCHA_TYPE_KEY); 
        
        // 输出图片
        captcha.write(response.getOutputStream());
    }

    //纯AI无人工
    @GetMapping("/galgame/generate")
    public Map<String, Object> generateGalGameCaptcha(HttpSession session) {
        Map<String, Object> result = userService.generateGalGameCaptcha();
        
        if (result != null) {
            session.setAttribute(Constant.CAPTCHA_TYPE_KEY, "GALGAME");
            session.setAttribute(Constant.GALGAME_TARGET_COLOR_KEY, result.get("targetColor"));
            session.setAttribute(Constant.GALGAME_CORRECT_INDEX_KEY, result.get("correctIndex"));
            
            // 为了安全和数据传输，移除不必要的字段
            Map<String, Object> response = new HashMap<>(result);
            response.remove("targetColor");
            response.remove("correctIndex");
            return response;
        }
        return null;
    }

    @PostMapping("/login")
    public AllExceptionResult login(String username, String password, String captcha, HttpSession session){
        String captchaType = (String) session.getAttribute(Constant.CAPTCHA_TYPE_KEY);
        Object storedCaptcha = session.getAttribute(Constant.CAPTCHA_SESSION_KEY);
        Long storedTime = (Long) session.getAttribute(Constant.CAPTCHA_TIME_KEY);
        Integer correctIndex = (Integer) session.getAttribute(Constant.GALGAME_CORRECT_INDEX_KEY);
        String targetColor = (String) session.getAttribute(Constant.GALGAME_TARGET_COLOR_KEY);

        // 调用Service层进行综合校验和登录
        AllExceptionResult result = userService.loginWithCaptcha(username, password, captcha, captchaType,
                                                                    storedCaptcha, storedTime, correctIndex, targetColor);

        if (result.getStatus() == loginAndResisterStatus.SUCCESS) {
            User user = (User) result.getData();
            //登录成功，存入Session (只存用户名和Id)
            session.setAttribute(Constant.USER_NAME, user.getUsername());
            session.setAttribute(Constant.USER_ID, user.getId());
            
            // 登录成功后清除验证码
            session.removeAttribute(Constant.CAPTCHA_SESSION_KEY);
            session.removeAttribute(Constant.CAPTCHA_TIME_KEY);
            session.removeAttribute(Constant.CAPTCHA_TYPE_KEY);
            session.removeAttribute(Constant.GALGAME_TARGET_COLOR_KEY);
            session.removeAttribute(Constant.GALGAME_CORRECT_INDEX_KEY);
        }
        
        return result;
    }

    @PostMapping("/register")
    public AllExceptionResult register(User user){
        //用户名，密码，昵称
        String username = user.getUsername();
        String password = user.getPassword();
        String nickname = user.getNickname();
        
        // 1. 通用校验
        AllExceptionResult checkResult = userService.checkUser(username, password);
        if (checkResult.getStatus() != loginAndResisterStatus.SUCCESS) {
            return checkResult;
        }
        
        // 2. 注册特有逻辑：昵称校验
        if (!userService.checkUser(nickname)) {
             //这里简单处理，如果昵称为空也返回参数错误
             return AllExceptionResult.nickNameError();
        }
        boolean result = userService.register(username,password,nickname);
        //判断注册成功与否
        if (result) {
            return AllExceptionResult.success();
        } else {
            //用户已存在
            return AllExceptionResult.userExists();
        }
    }

    //获取用户信息，查看是否登录
    @RequestMapping("/info")
    public User getUserInfo(HttpSession session){
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if(userId == null){
            //说明还未登录
            return null;
        }
        return userService.getById(userId);
    }

    // 检查登录状态 (返回 JSON)
    @RequestMapping("/check_login")
    public AllExceptionResult checkLogin(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            return AllExceptionResult.notLogin();
        }
        return AllExceptionResult.success();
    }

    //登出，并销毁Session
    @RequestMapping("/logout")
    public boolean logout(HttpSession session){
        //销毁
        session.invalidate();
        return true;
    }

    //用该用户信息接口，修改用户信息
    @PostMapping("/update")
    public boolean update(String nickname, MultipartFile avatarFile, HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) return false;

        // 修改昵称
        if (StringUtils.hasLength(nickname)) {
            // 校验昵称
            if (!userService.checkUser(nickname)) {
                return false;
            }
        }

        return userService.updateUserWithAvatar(userId, nickname, avatarFile);
    }

    // 修改密码
    @PostMapping("/updatePassword")
    public AllExceptionResult updatePassword(String oldPassword, String newPassword, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            return AllExceptionResult.userExists(); // 未登录
        }
        return userService.updatePassword(userId, oldPassword, newPassword);
    }

    /**
     * 获取粉丝榜 Top 10
     */
    @RequestMapping("/top10")
    public List<TopUserDTO> top10(HttpSession session){
        Long currentUserId = (Long)session.getAttribute(Constant.USER_ID);
        return userService.getTop10Authors(currentUserId);
    }
}