package org.example.treehole.service;

import cn.hutool.core.date.DateUtil;
import org.example.treehole.entry.DriftComment;
import org.example.treehole.entry.DriftHole;
import org.example.treehole.mapper.DriftCommentMapper;
import org.example.treehole.mapper.DriftHoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DriftHoleService {
    @Autowired
    private DriftHoleMapper driftHoleMapper;

    @Autowired
    private DriftCommentMapper driftCommentMapper;

    public DriftHole getRandomHole(Long viewerId) {
        DriftHole hole = driftHoleMapper.getRandomHole(viewerId);
        if (hole != null) {
            // 加载评论
            List<DriftComment> comments = driftCommentMapper.selectByHoleId(hole.getId());
            hole.setComments(comments);
        }
        return hole;
    }

    public boolean add(DriftHole hole) {
        if (hole == null || hole.getContent() == null || hole.getContent().trim().isEmpty()) {
            return false;
        }
        hole.setCreateTime(DateUtil.date());
        hole.setLikeCount(0);
        hole.setCommentCount(0);
        driftHoleMapper.insert(hole);
        return true;
    }

    @Transactional
    public boolean toggleLike(Long holeId, Long userId) {
        int count = driftHoleMapper.checkLike(holeId, userId);
        if (count > 0) {
            // 取消点赞
            driftHoleMapper.deleteLike(holeId, userId);
            driftHoleMapper.decrementLikeCount(holeId);
            return false; // 当前状态：未点赞
        } else {
            // 点赞
            driftHoleMapper.insertLike(holeId, userId);
            driftHoleMapper.incrementLikeCount(holeId);
            return true; // 当前状态：已点赞
        }
    }

    @Transactional
    public boolean addComment(DriftComment comment) {
        if (comment == null || comment.getContent() == null || comment.getContent().trim().isEmpty()) {
            return false;
        }
        comment.setCreateTime(DateUtil.date());
        driftCommentMapper.insert(comment);
        driftHoleMapper.incrementCommentCount(comment.getDriftHoleId());
        return true;
    }
}
