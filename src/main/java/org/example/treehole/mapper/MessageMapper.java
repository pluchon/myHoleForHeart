package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.treehole.entry.Message;

import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-22-16:02
 * 作者代码水平一般，难免难看，请见谅
 */
@Mapper
public interface MessageMapper {
    //发送私信，即把私信内容插入到表中
    @Insert("insert into message(sender_id, receiver_id, content, create_time) values(#{senderId}, #{receiverId}, #{content}, #{createTime})")
    int insert(Message message);

    // 查询与某人的聊天记录
    @Select("select * from message where (sender_id = #{userId1} and receiver_id = #{userId2}) or (sender_id = #{userId2} and receiver_id = #{userId1}) order by create_time asc")
    List<Message> selectHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 查询用户的所有相关消息（用于生成列表）
    @Select("select * from message where sender_id = #{userId} or receiver_id = #{userId} order by create_time desc")
    List<Message> selectByUserId(Long userId);
}
