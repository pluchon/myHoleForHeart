package org.example.treehole.controller;

import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.util.CaptchaUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    private static final String[] IMAGES = {
            "static/chatPicture/picture1.jpg",
            "static/chatPicture/picture2.jpg",
            "static/chatPicture/picture3.jpg",
            "static/chatPicture/picture4.jpg",
            "static/chatPicture/picture5.jpg",
            "static/chatPicture/picture6.jpg"
    };

    @GetMapping("/jigsaw")
    public Map<String, Object> getJigsaw(HttpSession session) throws IOException {
        String imagePath = RandomUtil.randomEle(IMAGES);
        Map<String, Object> data = CaptchaUtil.createJigsawCaptcha(imagePath);
        
        // 存储x坐标用于验证
        session.setAttribute(Constant.CAPTCHA_SESSION_KEY, String.valueOf(data.get("x")));
        session.setAttribute(Constant.CAPTCHA_TYPE_KEY, "jigsaw");
        session.setAttribute(Constant.CAPTCHA_TIME_KEY, System.currentTimeMillis());
        
        // 发送前移除敏感数据
        data.remove("x");
        // 保留y坐标用于前端定位
        return data;
    }

    @GetMapping("/textclick")
    public Map<String, Object> getTextClick(HttpSession session) throws IOException {
        String imagePath = RandomUtil.randomEle(IMAGES);
        Map<String, Object> data = CaptchaUtil.createTextClickCaptcha(imagePath);
        
        // 存储坐标用于验证
        session.setAttribute(Constant.CAPTCHA_SESSION_KEY, data.get("coords")); // List<Point>
        session.setAttribute(Constant.CAPTCHA_TYPE_KEY, "textclick");
        session.setAttribute(Constant.CAPTCHA_TIME_KEY, System.currentTimeMillis());
        
        data.remove("coords");
        return data;
    }
}
