package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.treehole.entry.TimeCapsule;

import java.util.List;

@Mapper
public interface TimeCapsuleMapper {

    //插入语句
    @Insert("INSERT INTO time_capsule(user_id, user_nickname, content, create_time, unlock_time, status) " +
            "VALUES(#{userId}, #{userNickname}, #{content}, #{createTime}, #{unlockTime}, 0)")
    void insert(TimeCapsule timeCapsule);

    // 查询用户所有的胶囊（包括未拆封的，用于展示列表）
    @Select("SELECT * FROM time_capsule WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TimeCapsule> selectByUserId(Long userId);

    // 查询用户已到期且未拆封的胶囊
    @Select("SELECT * FROM time_capsule WHERE user_id = #{userId} AND status = 0 AND unlock_time <= NOW()")
    List<TimeCapsule> selectUnlockable(Long userId);
}
