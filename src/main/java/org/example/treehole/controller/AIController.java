package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import lombok.Data;
import org.example.treehole.Constant;
import org.example.treehole.entry.AIMessage;
import org.example.treehole.entry.AllExceptionResult;
import org.example.treehole.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    @GetMapping("/history")
    public Object getHistory(@RequestParam String aiType, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllExceptionResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        return aiService.getHistory(userId, aiType);
    }

    @PostMapping("/chat")
    public Object chat(@RequestBody AIChatRequest request, HttpSession session) {
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        if (userIdObj == null) {
            return AllExceptionResult.notLogin();
        }
        Long userId = (Long) userIdObj;
        
        try {
            String response = aiService.chat(userId, request.getAiType(), request.getContent());
            return AllExceptionResult.successWithData(response);
        } catch (Exception e) {
            e.printStackTrace();
            return AllExceptionResult.aiError("AI 响应失败: " + e.getMessage());
        }
    }

    @Data
    public static class AIChatRequest {
        private String aiType;
        private String content;
    }
}
