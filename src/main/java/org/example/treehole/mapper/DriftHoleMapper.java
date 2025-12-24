package org.example.treehole.mapper;

import org.apache.ibatis.annotations.*;
import org.example.treehole.entry.DriftHole;

@Mapper
public interface DriftHoleMapper {
    /**
     * 随机获取一条漂流瓶
     * 同时查询当前用户是否点赞
     */
    @Select("SELECT d.*, " +
            "(SELECT COUNT(*) FROM drift_hole_like dl WHERE dl.drift_hole_id = d.id AND dl.user_id = #{viewerId}) > 0 AS isLiked " +
            "FROM drift_hole d ORDER BY RAND() LIMIT 1")
    DriftHole getRandomHole(@Param("viewerId") Long viewerId);

    @Insert("INSERT INTO drift_hole(content, user_id, create_time, like_count, comment_count) " +
            "VALUES(#{content}, #{userId}, #{createTime}, 0, 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DriftHole hole);

    // 点赞数+1
    @Update("UPDATE drift_hole SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(Long id);

    // 点赞数-1
    @Update("UPDATE drift_hole SET like_count = like_count - 1 WHERE id = #{id}")
    void decrementLikeCount(Long id);

    // 评论数+1
    @Update("UPDATE drift_hole SET comment_count = comment_count + 1 WHERE id = #{id}")
    void incrementCommentCount(Long id);

    // 插入点赞记录
    @Insert("INSERT INTO drift_hole_like(drift_hole_id, user_id) VALUES(#{holeId}, #{userId})")
    void insertLike(@Param("holeId") Long holeId, @Param("userId") Long userId);

    // 删除点赞记录
    @Delete("DELETE FROM drift_hole_like WHERE drift_hole_id = #{holeId} AND user_id = #{userId}")
    int deleteLike(@Param("holeId") Long holeId, @Param("userId") Long userId);

    // 检查是否点赞
    @Select("SELECT COUNT(*) FROM drift_hole_like WHERE drift_hole_id = #{holeId} AND user_id = #{userId}")
    int checkLike(@Param("holeId") Long holeId, @Param("userId") Long userId);
}
