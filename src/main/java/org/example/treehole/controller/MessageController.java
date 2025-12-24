package org.example.treehole.controller;

import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.AllResult;
import org.example.treehole.entry.Message;
import org.example.treehole.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author pluchon
 * @create 2025-12-22-16:06
 * 作者代码水平一般，难免难看，请见谅
 */
@RestController
@RequestMapping("/message")
public class MessageController {
    @Autowired
    private MessageService messageService;

    //发送私信，返回是否发送成功
    @RequestMapping("/send")
    public AllResult send(Long receiveId, String content, HttpSession session){
        Long senderId = (Long)session.getAttribute(Constant.USER_ID);
        if(senderId == null){
            //未登录
            return AllResult.notLogin();
        }
        //如果是自己给自己发私信，就不可以
        if(Objects.equals(receiveId, senderId)){
            return AllResult.sendMessageByMyself();
        }
        //检查内容长度
        if(content.length() < Constant.MESSAGE_MIN_LENGTH){
            //字数太少了
            return AllResult.textInSufficient();
        }
        if(content.length() > Constant.MESSAGE_MAX_LENGTH){
            //字数太多了
            return AllResult.textOver();
        }
        //接下来才是真正的发送成功
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiveId);
        message.setContent(content);
        boolean success = messageService.send(message);
        if(success){
            return AllResult.success();
        }
        return AllResult.textInSufficient(); // 发送失败暂且用这个或者通用错误
    }

    @RequestMapping("/conversations")
    public List<Map<String, Object>> conversations(HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) return Collections.emptyList();
        return messageService.getConversations(userId);
    }

    @RequestMapping("/history")
    public List<Message> history(Long otherUserId, HttpSession session) {
        Long userId = (Long) session.getAttribute(Constant.USER_ID);
        if (userId == null) return Collections.emptyList();
        return messageService.getHistory(userId, otherUserId);
    }

    //加载背景图片
    @RequestMapping("/backgrounds")
    public List<String> getBackgrounds() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:static/chatPicture/*");
            List<String> fileNames = new ArrayList<>();
            for (Resource r : resources) {
                fileNames.add(r.getFilename());
            }
            return fileNames;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
