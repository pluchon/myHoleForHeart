package org.example.treehole.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.Comment;
import org.example.treehole.entry.AllExceptionResult;
import org.example.treehole.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-18:53
 * 作者代码水平一般，难免难看，请见谅
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @RequestMapping("/add")
    public AllExceptionResult add(Long holeId, String content, HttpSession session){
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            return AllExceptionResult.notLogin();
        }

        Comment comment = new Comment();
        comment.setHoleId(holeId);
        comment.setContent(content);
        comment.setUserId(userId);
        commentService.addComment(comment);
        return AllExceptionResult.success();
    }

    @RequestMapping("/getComments")
    public List<Comment> getComments(Long holeId, HttpSession session){
        // 获取当前用户ID，用于判断是否已点赞
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        Long userId = (userIdObj != null) ? (Long) userIdObj : 0L;
        
        return commentService.getComments(holeId, userId);
    }

    @RequestMapping("/like")
    public boolean like(Long commentId, HttpSession session, HttpServletResponse response){
        // 获取当前用户ID
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            response.setStatus(401); // 未登录状态码
            return false; 
        }
        return commentService.like(commentId, userId);
    }
}
