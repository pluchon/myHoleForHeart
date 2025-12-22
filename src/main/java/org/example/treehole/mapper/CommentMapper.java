package org.example.treehole.mapper;

import org.apache.ibatis.annotations.*;
import org.example.treehole.entry.Comment;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-18:38
 * 作者代码水平一般，难免难看，请见谅
 */
@Mapper
public interface CommentMapper {
    //插入评论
    @Insert("insert into comment(hole_id,user_id,content,like_count) values(#{holeId},#{userId},#{content},0)")
    void insert(Comment comment);

    //查看某个树洞下的所有评论 (修改为包含点赞数和是否已赞状态)
    //在一次查询中，既拿到评论本身的信息，又知道“我”有没有给它点过赞
    @Select("select c.*, u.nickname as userNickname, u.avatar as userAvatar, " +
            "(select count(*) from comment_like cl where cl.comment_id = c.id and cl.user_id = #{userId}) > 0 as isLiked " +
            "from comment c " +
            "left join user u on c.user_id = u.id " +
            "where c.hole_id = #{holeId} order by c.id asc")
    @Results({
        @Result(property = "likeCount", column = "like_count"),
        @Result(property = "isLiked", column = "isLiked"),
        @Result(property = "userNickname", column = "userNickname"),
        @Result(property = "userAvatar", column = "userAvatar")
    })
    List<Comment> selectByHoleId(@Param("holeId") Long holeId, @Param("userId") Long userId);

    // 检查用户是否已赞某条评论
    @Select("select count(*) from comment_like where comment_id = #{commentId} and user_id = #{userId}")
    int checkLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    // 插入评论点赞记录
    @Insert("insert into comment_like(comment_id, user_id) values(#{commentId}, #{userId})")
    void insertLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    // 删除评论点赞记录
    @Delete("delete from comment_like where comment_id = #{commentId} and user_id = #{userId}")
    void deleteLike(@Param("commentId") Long commentId, @Param("userId") Long userId);

    // 更新评论点赞数
    @Update("update comment set like_count = like_count + 1 where id = #{id}")
    void updateLikeCount(@Param("id") Long id);

    // 减少评论点赞数
    @Update("update comment set like_count = like_count - 1 where id = #{id} and like_count > 0")
    void decreaseLikeCount(@Param("id") Long id);
}
