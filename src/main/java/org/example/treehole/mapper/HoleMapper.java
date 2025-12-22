package org.example.treehole.mapper;

import org.apache.ibatis.annotations.*;
import org.example.treehole.entry.Hole;
import org.example.treehole.entry.LikeMessage;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-16:55
 * 作者代码水平一般，难免难看，请见谅
 */
@Mapper
public interface HoleMapper {
    //插入一条树树洞语句
    @Insert("insert into hole(user_id,content,category) values(#{userId},#{content},#{category})")
    void insert(Hole hole);

    //展示树洞所有信息 (支持分类筛选)。动态SQL
    List<Hole> queryAll(@Param("offset") Integer offset, @Param("limit") Integer limit,
                        @Param("userId") Long userId, @Param("category") String category);

    //点赞+1，根据内容的Id来寻找
    @Update("update hole set like_count = like_count+1 where id = #{id}")
    boolean updateLikeCount(Long id);

    //点赞-1
    @Update("update hole set like_count = like_count-1 where id = #{id}")
    boolean decreaseLikeCount(Long id);

    // 检查是否已点赞
    @Select("select count(*) from hole_like where hole_id = #{holeId} and user_id = #{userId}")
    int checkLike(@Param("holeId") Long holeId, @Param("userId") Long userId);

    // 插入点赞记录
    @Insert("insert into hole_like(hole_id, user_id) values(#{holeId}, #{userId})")
    void insertLike(@Param("holeId") Long holeId, @Param("userId") Long userId);

    // 删除点赞记录
    @Delete("delete from hole_like where hole_id = #{holeId} and user_id = #{userId}")
    void deleteLike(@Param("holeId") Long holeId, @Param("userId") Long userId);

    //根据用户Id查询树洞内容，并包含当前查看用户的点赞状态
    @Select("select h.*, u.nickname as userNickname, u.avatar as userAvatar, " +
            "(select count(*) from hole_like hl where hl.hole_id = h.id and hl.user_id = #{viewerId}) > 0 as isLiked " +
            "from hole h left join `user` u on h.user_id = u.id " +
            "where h.user_id = #{userId} order by h.id desc")
    List<Hole> queryByUserId(@Param("userId") Long userId, @Param("viewerId") Long viewerId);

    //评论数+1
    @Update("update hole set comment_count = comment_count + 1 where id = #{id}")
    void updateCommentCount(Long id);

    //登录用户删除自己的树洞
    @Delete("delete from hole where id = #{id} and user_id = #{userId}")
    Integer deleteByIdAndUserId(long id,Long userId);

    // 查询给我点赞的消息：查询所有点赞了我的树洞的用户（排除自己给自己点赞的情况）
    @Select("select u.id as userId, u.nickname, u.avatar, h.content as holeContent " +
            "from hole_like hl " +
            "left join user u on hl.user_id = u.id " +
            "left join hole h on hl.hole_id = h.id " +
            "where h.user_id = #{userId} and hl.user_id != #{userId} " +
            "order by hl.hole_id desc")
    List<LikeMessage> selectLikeMessage(Long userId);
}