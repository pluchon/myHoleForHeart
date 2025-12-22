package org.example.treehole.service;

import org.example.treehole.entry.Message;
import org.example.treehole.entry.User;
import org.example.treehole.mapper.MessageMapper;
import org.example.treehole.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author pluchon
 * @create 2025-12-22-16:03
 * 作者代码水平一般，难免难看，请见谅
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper mapper;

    @Autowired
    private UserMapper userMapper;

    //发送私信，如果＞0说明发送成功
    public boolean send(Message message){
        message.setCreateTime(new Date()); // Ensure time is set
        int result = mapper.insert(message);
        return result > 0;
    }

    public List<Message> getHistory(Long u1, Long u2) {
        return mapper.selectHistory(u1, u2);
    }

    //获取私信内容
    public List<Map<String, Object>> getConversations(Long userId) {
        List<Message> allMessages = mapper.selectByUserId(userId);
        Map<Long, Message> latestMap = new LinkedHashMap<>(); // 如果可能的话，使用LinkedHashMap保持顺序，但我们还是会在后面排序

        for (Message m : allMessages) {
            Long otherId = m.getSenderId().equals(userId) ? m.getReceiverId() : m.getSenderId();
            if (!latestMap.containsKey(otherId)) {
                latestMap.put(otherId, m); // 由于列表按 decc 排序，第一个是最新的
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : latestMap.entrySet()) {
            Long otherId = entry.getKey();
            Message msg = entry.getValue();
            User user = userMapper.queryById(otherId);
            if (user != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("userId", user.getId());
                map.put("nickname", user.getNickname());
                map.put("avatar", user.getAvatar());
                map.put("lastMessage", msg.getContent());
                map.put("lastTime", msg.getCreateTime());
                result.add(map);
            }
        }
        return result;
    }
}
