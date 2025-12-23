package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.LoginAndResisterResult;
import org.example.treehole.entry.User;
import org.example.treehole.entry.UserFollow;
import org.example.treehole.enums.loginAndResisterStatus;
import org.example.treehole.service.UserFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户关注控制器
 * 提供关注相关的 API 接口
 */
@RestController
@RequestMapping("/follow")
public class UserFollowController {

    @Autowired
    private UserFollowService userFollowService;

    /**
     * 切换关注状态 (POST /follow/toggle)
     * @param followedId 被关注者ID
     * @param session 当前会话
     * @return LoginAndResisterResult
     */
    @PostMapping("/toggle")
    public LoginAndResisterResult toggleFollow(@RequestParam Long followedId, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        
        // 登录校验
        if (userId == null) {
            return LoginAndResisterResult.notLogin();
        }
        
        // 自我关注校验
        if (userId.equals(followedId)) {
            return LoginAndResisterResult.followMyself();
        }

        try {
            boolean isFollowing = userFollowService.toggleFollow(userId, followedId);
            LoginAndResisterResult result = LoginAndResisterResult.success();
            result.setErrorMessage(isFollowing ? "关注成功" : "已取消关注");
            
            Map<String, Object> data = new HashMap<>();
            data.put("isFollowing", isFollowing);
            result.setData(data);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return LoginAndResisterResult.followOpFailed("操作失败: " + e.getMessage());
        }
    }

    /**
     * 切换特别关注状态 (POST /follow/toggleSpecial)
     * @param followedId 被关注者ID
     * @param session 当前会话
     * @return LoginAndResisterResult
     */
    @PostMapping("/toggleSpecial")
    public LoginAndResisterResult toggleSpecial(@RequestParam Long followedId, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        
        if (userId == null) {
            return LoginAndResisterResult.notLogin();
        }
        
        // 检查是否已关注
        UserFollow uf = userFollowService.getFollowStatus(userId, followedId);
        if (uf == null) {
            return LoginAndResisterResult.notFollowed();
        }

        try {
            boolean isSpecial = userFollowService.toggleSpecial(userId, followedId);
            LoginAndResisterResult result = LoginAndResisterResult.success();
            result.setErrorMessage(isSpecial ? "已设为特别关注" : "已取消特别关注");
            
            Map<String, Object> data = new HashMap<>();
            data.put("isSpecial", isSpecial);
            result.setData(data);
            
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return LoginAndResisterResult.followOpFailed("操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的关注列表 (GET /follow/my)
     * @param session 当前会话
     * @return 关注列表
     */
    @GetMapping("/my")
    public List<UserFollow> getMyFollows(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            return new ArrayList<>();
        }
        return userFollowService.getMyFollows(userId);
    }

    /**
     * 获取对某用户的关注状态 (GET /follow/status)
     * @param followedId 目标用户ID
     * @param session 当前会话
     * @return LoginAndResisterResult
     */
    @GetMapping("/status")
    public LoginAndResisterResult getStatus(@RequestParam Long followedId, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        Map<String, Object> data = new HashMap<>();
        
        if (userId == null) {
            data.put("isFollowing", false);
            data.put("isSpecial", false);
        } else {
            UserFollow uf = userFollowService.getFollowStatus(userId, followedId);
            data.put("isFollowing", uf != null);
            data.put("isSpecial", uf != null && uf.getIsSpecial() == 1);
        }
        
        LoginAndResisterResult result = LoginAndResisterResult.success();
        result.setData(data);
        return result;
    }
}