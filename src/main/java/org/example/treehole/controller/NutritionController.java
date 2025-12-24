package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.AllResult;
import org.example.treehole.entry.UserNutrition;
import org.example.treehole.service.NutritionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/nutrition")
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    @GetMapping("/info")
    public AllResult getInfo(HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        UserNutrition info = nutritionService.getInfo(userId);
        
        return AllResult.successWithData(info);
    }

    @PostMapping("/check-in")
    public AllResult checkIn(HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        return nutritionService.checkIn(userId);
    }

    @GetMapping("/month-log")
    public AllResult getMonthLog(HttpSession session, @RequestParam String yearMonth) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        List<String> logs = nutritionService.getMonthlyCheckInLog(userId, yearMonth);
        return AllResult.successWithData(logs);
    }
}
