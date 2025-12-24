package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.AnonymousHole;
import org.example.treehole.entry.AllExceptionResult;
import org.example.treehole.service.AnonymousHoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/anonymous")
public class AnonymousHoleController {

    @Autowired
    private AnonymousHoleService anonymousHoleService;

    @GetMapping("/random")
    public AnonymousHole getRandomHole() {
        return anonymousHoleService.getRandomHole();
    }

    @PostMapping("/publish")
    public AllExceptionResult publish(@RequestBody AnonymousHole hole, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllExceptionResult.notLogin();
        }

        // 校验内容
        if (hole.getContent() == null || hole.getContent().trim().length() < 5) {
            return AllExceptionResult.textInSufficient();
        }

        boolean success = anonymousHoleService.add(hole);
        if (success) {
            AllExceptionResult result = AllExceptionResult.success();
            result.setErrorMessage("投递成功，秘密已封存");
            return result;
        } else {
            return AllExceptionResult.anonymousPublishFailed();
        }
    }
}
