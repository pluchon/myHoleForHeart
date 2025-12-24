package org.example.treehole.controller;

import cn.hutool.core.util.ObjectUtil;
import org.example.treehole.Constant;
import org.example.treehole.entry.AllExceptionResult;
import org.example.treehole.entry.TimeCapsule;
import org.example.treehole.entry.User;
import org.example.treehole.service.TimeCapsuleService;
import org.example.treehole.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/capsule")
public class TimeCapsuleController {

    @Autowired
    private TimeCapsuleService timeCapsuleService;

    @Autowired
    private UserService userService;

    // 埋藏胶囊
    @PostMapping("/bury")
    public AllExceptionResult bury(@RequestBody TimeCapsule capsule, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        // 使用 Hutool 判断用户ID是否为空
        if (ObjectUtil.isNull(userId)) {
            return AllExceptionResult.notLogin();
        }

        User user = userService.getById(userId);
        if (user == null) {
            return AllExceptionResult.notLogin();
        }

        capsule.setUserId(user.getId());
        capsule.setUserNickname(user.getNickname());
        
        boolean success = timeCapsuleService.buryCapsule(capsule);

        if (success) {
            return AllExceptionResult.success();
        } else {
            return AllExceptionResult.timeCapsuleError();
        }
    }

    // 查看我的胶囊列表
    @GetMapping("/list")
    public List<TimeCapsule> list(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (ObjectUtil.isNull(userId)) {
            return null;
        }
        return timeCapsuleService.getMyCapsules(userId);
    }
}
