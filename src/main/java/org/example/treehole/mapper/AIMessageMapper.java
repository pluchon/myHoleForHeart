package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.treehole.entry.AIMessage;

import java.util.List;

@Mapper
public interface AIMessageMapper {

    @Insert("INSERT INTO ai_message (user_id, ai_type, sender, content, create_time) " +
            "VALUES (#{userId}, #{aiType}, #{sender}, #{content}, NOW())")
    void insert(AIMessage message);

    @Select("SELECT * FROM ai_message WHERE user_id = #{userId} AND ai_type = #{aiType} ORDER BY create_time ASC")
    List<AIMessage> selectHistory(Long userId, String aiType);
}
