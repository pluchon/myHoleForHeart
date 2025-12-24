package org.example.treehole.service;

import org.example.treehole.entry.UserFollow;
import org.example.treehole.mapper.UserFollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 用户关注服务类
 * 处理关注业务逻辑
 */
@Service
public class UserFollowService {

    @Autowired
    private UserFollowMapper userFollowMapper;

    /**
     * 切换关注状态 (关注/取消关注)
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return true: 关注成功, false: 取消关注
     */
    public boolean toggleFollow(Long followerId, Long followedId) {
        // 检查是否已关注
        if (userFollowMapper.count(followerId, followedId) > 0) {
            // 已关注 -> 取消关注
            userFollowMapper.delete(followerId, followedId);
            return false;
        } else {
            // 未关注 -> 添加关注
            UserFollow uf = new UserFollow();
            uf.setFollowerId(followerId);
            uf.setFollowedId(followedId);
            uf.setIsSpecial(0); // 默认非特别关注
            userFollowMapper.insert(uf);
            return true;
        }
    }

    /**
     * 切换特别关注状态
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return true: 设置为特别关注, false: 取消特别关注
     */
    public boolean toggleSpecial(Long followerId, Long followedId) {
        UserFollow uf = userFollowMapper.selectOne(followerId, followedId);
        if (uf != null) {
            int newStatus = (uf.getIsSpecial() == 1) ? 0 : 1;
            userFollowMapper.updateSpecial(followerId, followedId, newStatus);
            return newStatus == 1;
        }
        return false;
    }

    /**
     * 获取指定用户的关注列表
     * @param userId 用户ID
     * @return 关注列表
     */
    public List<UserFollow> getMyFollows(Long userId) {
        return userFollowMapper.selectByFollowerId(userId);
    }
    
    /**
     * 获取指定用户的粉丝列表
     * @param userId 用户ID
     * @return 粉丝列表
     */
    public List<UserFollow> getMyFans(Long userId) {
        return userFollowMapper.selectFansByUserId(userId);
    }
    
    /**
     * 检查关注状态
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注记录实体 (若未关注则返回null)
     */
    public UserFollow getFollowStatus(Long followerId, Long followedId) {
        return userFollowMapper.selectOne(followerId, followedId);
    }
}
