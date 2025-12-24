package org.example.treehole.mapper;

import org.apache.ibatis.annotations.*;
import org.example.treehole.entry.UserFollow;
import java.util.List;

/**
 * 用户关注 Mapper 接口
 * 负责 user_follow 表的 CRUD 操作
 */
@Mapper
public interface UserFollowMapper {
    
    /**
     * 插入关注记录
     */
    @Insert("INSERT INTO user_follow(follower_id, followed_id, is_special, create_time) VALUES(#{followerId}, #{followedId}, #{isSpecial}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(UserFollow userFollow);

    /**
     * 删除关注记录 (取消关注)
     */
    @Delete("DELETE FROM user_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    void delete(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * 统计关注记录数 (判断是否已关注)
     */
    @Select("SELECT count(*) FROM user_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    int count(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * 更新特别关注状态
     */
    @Update("UPDATE user_follow SET is_special = #{isSpecial} WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    void updateSpecial(@Param("followerId") Long followerId, @Param("followedId") Long followedId, @Param("isSpecial") Integer isSpecial);

    /**
     * 获取我的关注列表
     * 关联查询用户信息，按特别关注优先，然后时间倒序排列
     */
    @Select("SELECT f.*, u.nickname as followedNickname, u.avatar as followedAvatar " +
            "FROM user_follow f " +
            "LEFT JOIN user u ON f.followed_id = u.id " +
            "WHERE f.follower_id = #{followerId} " +
            "ORDER BY f.is_special DESC, f.create_time DESC")
    List<UserFollow> selectByFollowerId(Long followerId);
    
    /**
     * 查询单条关注记录
     */
    @Select("SELECT * FROM user_follow WHERE follower_id = #{followerId} AND followed_id = #{followedId}")
    UserFollow selectOne(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * 获取我的粉丝列表
     * 关联查询粉丝信息，并判断是否互相关注
     */
    @Select("SELECT f.*, u.nickname as followerNickname, u.avatar as followerAvatar, " +
            "(SELECT count(*) FROM user_follow f2 WHERE f2.follower_id = f.followed_id AND f2.followed_id = f.follower_id) > 0 as isMutual " +
            "FROM user_follow f " +
            "LEFT JOIN user u ON f.follower_id = u.id " +
            "WHERE f.followed_id = #{userId} " +
            "ORDER BY f.create_time DESC")
    List<UserFollow> selectFansByUserId(Long userId);
}
