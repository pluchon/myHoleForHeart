package org.example.treehole.service;

import org.example.treehole.entry.Comment;
import org.example.treehole.mapper.CommentMapper;
import org.example.treehole.mapper.HoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-18:41
 * 作者代码水平一般，难免难看，请见谅
 */
@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private HoleMapper holeMapper;

    //添加评论
    public void addComment(Comment comment){
        //插入评论
        commentMapper.insert(comment);
        //给对应树洞的评论数+1
        holeMapper.updateCommentCount(comment.getHoleId());
    }

    //返回评论列表
    public List<Comment> getComments(Long holeId, Long userId){
        return commentMapper.selectByHoleId(holeId, userId);
    }

    // 评论点赞 (切换状态)
    // 返回值：true 表示操作后状态为已赞，false 表示操作后状态为未赞
    public boolean like(Long commentId, Long userId) {
        // 1. 基础校验
        if (userId == null || userId == 0) return false;

        // 2. 检查是否已经点赞
        int count = commentMapper.checkLike(commentId, userId);
        
        if (count > 0) {
            // 3. 如果已经点赞，则取消点赞
            commentMapper.deleteLike(commentId, userId);
            commentMapper.decreaseLikeCount(commentId);
            return false; // 当前状态：未赞
        } else {
            // 4. 如果未点赞，则进行点赞
            commentMapper.insertLike(commentId, userId);
            commentMapper.updateLikeCount(commentId);
            return true; // 当前状态：已赞
        }
    }
}
