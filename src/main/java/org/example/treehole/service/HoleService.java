package org.example.treehole.service;

import org.example.treehole.Constant;
import org.example.treehole.entry.Hole;
import org.example.treehole.entry.LikeMessage;
import org.example.treehole.mapper.HoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-16:59
 * 作者代码水平一般，难免难看，请见谅
 */
@Service
public class HoleService {
    @Autowired
    private HoleMapper holeMapper;

    //发布树洞内容
    public boolean create(Hole hole){
        //校验 (修正为 || 逻辑)
        if(!StringUtils.hasLength(hole.getContent()) || !StringUtils.hasLength(hole.getCategory())){
            return false;
        }
        //发布成功
        holeMapper.insert(hole);
        return true;
    }

    //展示树洞所有内容
    public List<Hole> queryAllContent(Integer page, Long userId, String category){
        //我们计算当前页内容从哪一条树洞信息开始
        int offset = (page - 1) * Constant.PAGE_SIZE;
        return holeMapper.queryAll(offset, Constant.PAGE_SIZE, userId, category);
    }

    //点赞 (切换状态)
    public boolean like(Long id, Long userId){
        // 1. 检查是否已经点赞
        if (userId == null || userId == 0) return false;
        
        int count = holeMapper.checkLike(id, userId);
        if (count > 0) {
            // 已经点赞过了 -> 取消点赞
            holeMapper.deleteLike(id, userId);
            holeMapper.decreaseLikeCount(id);
            return false; // 返回当前状态：未点赞
        } else {
            // 未点赞 -> 添加点赞
            holeMapper.insertLike(id, userId);
            holeMapper.updateLikeCount(id);
            return true; // 返回当前状态：已点赞
        }
    }

    //根据用户Id查询树洞发布内容，userId为目标用户，viewerId为查看者（用于判断是否点赞）
    public List<Hole> queryByUserId(Long userId, Long viewerId){
        return holeMapper.queryByUserId(userId, viewerId);
    }

    //登录用户删除自己发布的树洞
    public boolean deleteByUser(Long id,Long userId){
        //只有成功删除了才会返回大于1的内容
        return holeMapper.deleteByIdAndUserId(id,userId) > 0;
    }

    // 获取点赞前10的树洞
    public List<Hole> getTop10Liked(Long viewerId) {
        return holeMapper.queryTop10Liked(viewerId);
    }

    //获取点赞的消息列表
    public List<LikeMessage> getLikeMessage(Long userId){
        return holeMapper.selectLikeMessage(userId);
    }
}