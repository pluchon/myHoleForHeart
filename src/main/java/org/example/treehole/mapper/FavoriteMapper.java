package org.example.treehole.mapper;

import org.apache.ibatis.annotations.*;
import org.example.treehole.entry.Favorite;
import org.example.treehole.entry.Hole;

import java.util.List;

@Mapper
public interface FavoriteMapper {
    @Insert("insert into favorite(user_id, hole_id) values(#{userId}, #{holeId})")
    void insert(Favorite favorite);

    @Delete("delete from favorite where user_id = #{userId} and hole_id = #{holeId}")
    void delete(@Param("userId") Long userId, @Param("holeId") Long holeId);

    @Select("select count(*) from favorite where user_id = #{userId} and hole_id = #{holeId}")
    int count(@Param("userId") Long userId, @Param("holeId") Long holeId);

    // List favorites. Note: we manually set isFavorited to true since it's the favorite list.
    @Select("select h.*, u.nickname as userNickname, u.avatar as userAvatar, " +
            "(select count(*) from hole_like hl where hl.hole_id = h.id and hl.user_id = #{userId}) > 0 as isLiked, " +
            "true as isFavorited " +
            "from favorite f " +
            "left join hole h on f.hole_id = h.id " +
            "left join user u on h.user_id = u.id " +
            "where f.user_id = #{userId} " +
            "order by f.create_time desc")
    List<Hole> selectByUserId(@Param("userId") Long userId);
}
