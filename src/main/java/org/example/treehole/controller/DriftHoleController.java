package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.DriftComment;
import org.example.treehole.entry.DriftHole;
import org.example.treehole.entry.AllResult;
import org.example.treehole.service.DriftHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/drift")
public class DriftHoleController {

    @Autowired
    private DriftHoleService driftHoleService;

    @GetMapping("/random")
    public DriftHole getRandomHole(HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        Long userId = (userIdObj != null) ? (Long) userIdObj : 0L;
        return driftHoleService.getRandomHole(userId);
    }

    @PostMapping("/publish")
    public AllResult publish(@RequestBody DriftHole hole, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        hole.setUserId(userId);

        // 校验内容
        if (hole.getContent() == null || hole.getContent().trim().length() < 5) {
            return AllResult.textInSufficient();
        }

        boolean success = driftHoleService.add(hole);
        if (success) {
            AllResult result = AllResult.success();
            result.setErrorMessage("漂流瓶已扔出，等待有缘人拾取");
            return result;
        } else {
            return AllResult.anonymousPublishFailed();
        }
    }

    @PostMapping("/like")
    public AllResult like(@RequestParam Long holeId, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;

        boolean isLiked = driftHoleService.toggleLike(holeId, userId);
        Map<String, Object> data = new HashMap<>();
        data.put("isLiked", isLiked);
        return AllResult.successWithData(data);
    }

    @PostMapping("/comment")
    public AllResult comment(@RequestBody DriftComment comment, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        comment.setUserId(userId);

        if (comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return AllResult.textInSufficient();
        }

        boolean success = driftHoleService.addComment(comment);
        if (success) {
            return AllResult.success();
        } else {
            return AllResult.fail();
        }
    }
}
