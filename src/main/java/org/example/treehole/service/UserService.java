package org.example.treehole.service;

import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ReUtil;
import org.example.treehole.Constant;
import org.example.treehole.entry.LoginAndResisterResult;
import org.example.treehole.entry.User;
import org.example.treehole.enums.loginAndResisterStatus;
import org.example.treehole.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author pluchon
 * @create 2025-12-20-16:14
 * 作者代码水平一般，难免难看，请见谅
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    // 通用校验逻辑
    public LoginAndResisterResult checkUser(String username, String password) {
        //1. 校验非法字符 (使用 Hutool Validator)
        // 用户名和密码只允许：字母、数字、下划线 (isGeneral)
        if (!Validator.isGeneral(username) || !Validator.isGeneral(password)) {
            return LoginAndResisterResult.illegalCharacter();
        }

        //2. 超过规定长度
        if(username.length() > Constant.MAX_LENGTH || password.length() > Constant.MAX_LENGTH){
            return LoginAndResisterResult.overLength();
        }

        //3. 不足规定长度
        if(username.length() < Constant.MIN_LENGTH || password.length() < Constant.MIN_LENGTH){
            return LoginAndResisterResult.inSufficientLength();
        }
        
        // 校验通过
        return LoginAndResisterResult.success();
    }

    // 校验昵称 (单独的方法，因为昵称允许中文)
    public boolean checkUser(String nickname) {
        // 1. 长度校验
        if (nickname.length() > Constant.MAX_LENGTH || nickname.length() < Constant.MIN_LENGTH) {
            return false;
        }
        // 2. 非法字符校验 (Hutool)
        // 昵称允许：中文、字母、数字、下划线
        return ReUtil.isMatch("^[\\u4e00-\\u9fa5\\w]+$", nickname);
    }

    //登录逻辑
    public User login(String username,String password){
        if(!StringUtils.hasLength(username) || !StringUtils.hasLength(password)){
            return null;
        }
        User isExists = userMapper.findByUsername(username);
        //如果不存在返回false
        if(isExists == null){
            return null;
        }
        //此时用户名存在，要进行密码比对
        if(!isExists.getPassword().equals(password)){
            return null;
        }
        //接下来就是登录成功了
        return isExists;
    }

    //注册逻辑
    public boolean register(String username,String password,String nickname){
        //合法性校验
        if(!StringUtils.hasLength(username) || !StringUtils.hasLength(password) || !StringUtils.hasLength(nickname)){
            return false;
        }
        // 查重逻辑
        User existUser = userMapper.findByUsername(username);
        if(existUser != null){
            return false;
        }

        //注册成功
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setNickname(nickname);
        userMapper.insert(newUser);
        return true;
    }

    public User getById(Long userId) {
        return userMapper.queryById(userId);
    }

    //添加用户信息更新
    public boolean updateUserInfo(User user){
        if(user.getId() == null){
            return false;
        }
        userMapper.update(user);
        return true;
    }

    // 综合登录校验逻辑
    public LoginAndResisterResult loginWithCaptcha(String username, String password, String captcha, String captchaType, 
                                                   Object storedCaptchaObj, Long storedTime, Integer correctIndex, String targetColor) {
        // 1. 通用校验
        LoginAndResisterResult checkResult = checkUser(username, password);
        if (checkResult.getStatus() != loginAndResisterStatus.SUCCESS) {
            return checkResult;
        }

        // 2. 验证码校验
        if ("GALGAME".equals(captchaType)) {
             if (correctIndex == null) {
                 return LoginAndResisterResult.captchaIllegal();
             }
             try {
                 int userIndex = Integer.parseInt(captcha);
                 if (userIndex != correctIndex) {
                      LoginAndResisterResult res = LoginAndResisterResult.captchaError();
                      res.setErrorMessage("杂鱼，连" + (targetColor!=null?targetColor:"") + "女孩子都分辨不出来了吗");
                      return res;
                 }
             } catch (NumberFormatException e) {
                 return LoginAndResisterResult.captchaError();
             }
        } else if ("jigsaw".equals(captchaType)) {
            if (storedCaptchaObj == null || storedTime == null) return LoginAndResisterResult.captchaIllegal();
            try {
                int x = Integer.parseInt(captcha); // 用户发送的x偏移量
                int storedX = Integer.parseInt(storedCaptchaObj.toString());
                if (Math.abs(x - storedX) > 5) { // 5px误差允许
                    return LoginAndResisterResult.captchaError();
                }
            } catch (Exception e) {
                return LoginAndResisterResult.captchaError();
            }
        } else if ("textclick".equals(captchaType)) {
            if (storedCaptchaObj == null || !(storedCaptchaObj instanceof java.util.List)) return LoginAndResisterResult.captchaIllegal();
            try {
                java.util.List<java.awt.Point> storedPoints = (java.util.List<java.awt.Point>) storedCaptchaObj;
                String[] userPoints = captcha.split(";");
                if (userPoints.length != storedPoints.size()) return LoginAndResisterResult.captchaError();
                
                for (int i = 0; i < storedPoints.size(); i++) {
                    String[] coords = userPoints[i].split(",");
                    int ux = Integer.parseInt(coords[0]);
                    int uy = Integer.parseInt(coords[1]);
                    java.awt.Point sp = storedPoints.get(i);
                    // 距离校验（半径30px）
                    if (Math.pow(ux - sp.x, 2) + Math.pow(uy - sp.y, 2) > 900) {
                         return LoginAndResisterResult.captchaError();
                    }
                }
            } catch (Exception e) {
                return LoginAndResisterResult.captchaError();
            }
        } else {
            String storedCaptcha = (storedCaptchaObj instanceof String) ? (String) storedCaptchaObj : null;
            if (!StringUtils.hasLength(captcha) || !StringUtils.hasLength(storedCaptcha) || storedTime == null) {
                return LoginAndResisterResult.captchaIllegal();
            }
            if (System.currentTimeMillis() - storedTime > Constant.CAPTCHA_EXPIRATION_TIME) {
                return LoginAndResisterResult.captchaTimeOut();
            }
            if (!storedCaptcha.equalsIgnoreCase(captcha)) {
                return LoginAndResisterResult.captchaError();
            }
        }

        // 3. 用户登录
        User user = login(username, password);
        if (user == null) {
            return LoginAndResisterResult.userExists();
        }

        // 登录成功
        LoginAndResisterResult success = LoginAndResisterResult.success();
        success.setData(user);
        if ("GALGAME".equals(captchaType)) {
            success.setErrorMessage("提示：你真的是GalGame高手");
        }
        return success;
    }

    // 生成GalGame验证码数据
    public Map<String, Object> generateGalGameCaptcha() {
        String projectPath = System.getProperty("user.dir");
        String path = projectPath + "/src/main/resources/static/captchaForGal";
        File dir = new File(path);
        File[] files = dir.listFiles();
        
        if (files == null || files.length == 0) {
            return null;
        }

        Map<String, List<String>> colorMap = new HashMap<>();
        for (File f : files) {
            String name = f.getName();
            if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpeg")) {
                String color = name.replaceAll("\\d+\\.(jpg|png|jpeg)$", "");
                colorMap.computeIfAbsent(color, k -> new ArrayList<>()).add(name);
            }
        }
        
        List<String> colors = new ArrayList<>(colorMap.keySet());
        if (colors.isEmpty()) return null;

        Random random = new Random();
        String targetColor = colors.get(random.nextInt(colors.size()));
        
        List<String> targetImages = colorMap.get(targetColor);
        String targetImage = targetImages.get(random.nextInt(targetImages.size()));
        
        List<String> distractors = new ArrayList<>();
        List<String> otherColors = new ArrayList<>(colors);
        otherColors.remove(targetColor);
        
        for (int i = 0; i < 2; i++) {
             if (otherColors.isEmpty()) break;
             String otherColor = otherColors.get(random.nextInt(otherColors.size()));
             List<String> otherImages = colorMap.get(otherColor);
             if (otherImages != null && !otherImages.isEmpty()) {
                 distractors.add(otherImages.get(random.nextInt(otherImages.size())));
             }
        }
        
        while(distractors.size() < 2) {
             List<String> allImages = new ArrayList<>();
             for(List<String> l : colorMap.values()) allImages.addAll(l);
             allImages.remove(targetImage);
             allImages.removeAll(distractors);
             
             if(allImages.isEmpty()) break;
             distractors.add(allImages.get(random.nextInt(allImages.size())));
        }
        
        List<String> finalImages = new ArrayList<>();
        finalImages.add(targetImage);
        finalImages.addAll(distractors);
        
        Collections.shuffle(finalImages);
        
        int correctIndex = finalImages.indexOf(targetImage);
        
        Map<String, Object> result = new HashMap<>();
        result.put("question", "请找出图片中" + targetColor + "女孩子");
        List<String> imageUrls = new ArrayList<>();
        for (String img : finalImages) {
            imageUrls.add("/captchaForGal/" + img);
        }
        result.put("images", imageUrls);
        result.put("targetColor", targetColor);
        result.put("correctIndex", correctIndex);
        
        return result;
    }

    // 处理用户头像更新
    public boolean updateUserWithAvatar(Long userId, String nickname, MultipartFile avatarFile) throws IOException {
        User user = getById(userId);
        if (user == null) return false;

        if (StringUtils.hasLength(nickname)) {
            user.setNickname(nickname);
        }

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String originalFilename = avatarFile.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = UUID.randomUUID().toString() + suffix;

            String projectPath = System.getProperty("user.dir");
            String staticPath = projectPath + "/src/main/resources/static/avatars/";
            String targetPath = projectPath + "/target/classes/static/avatars/";

            // 保存到源码目录 (持久化)
            File dir = new File(staticPath);
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, newFileName);
            avatarFile.transferTo(dest);
            
            // 保存到运行目录 (立即生效)
            try {
                File targetDir = new File(targetPath);
                if (!targetDir.exists()) targetDir.mkdirs();
                // 简单的文件复制
                java.nio.file.Files.copy(dest.toPath(), new File(targetDir, newFileName).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                // 忽略运行目录复制错误 (可能是在非IDE环境运行)
                e.printStackTrace();
            }
            user.setAvatar("/avatars/" + newFileName);
        }

        return updateUserInfo(user);
    }

    // 修改密码逻辑
    public LoginAndResisterResult updatePassword(Long userId, String oldPassword, String newPassword) {
        if (!StringUtils.hasLength(oldPassword) || !StringUtils.hasLength(newPassword)) {
            return LoginAndResisterResult.digit(); // 简单复用参数错误的返回
        }

        User user = getById(userId);
        if (user == null) {
            return LoginAndResisterResult.userExists(); // 复用用户错误
        }

        // 验证旧密码
        if (!user.getPassword().equals(oldPassword)) {
            LoginAndResisterResult result = new LoginAndResisterResult();
            result.setStatus(loginAndResisterStatus.PASSWORDERROR);
            result.setErrorMessage("旧密码错误");
            return result;
        }

        // 验证新密码格式
        //复用 checkUser 逻辑，传入 username 作为占位符
        LoginAndResisterResult checkResult = checkUser("username", newPassword);
        if (checkResult.getStatus() != loginAndResisterStatus.SUCCESS) {
            return checkResult;
        }

        // 修改密码
        userMapper.updatePassword(userId, newPassword);
        return LoginAndResisterResult.success();
    }
}
