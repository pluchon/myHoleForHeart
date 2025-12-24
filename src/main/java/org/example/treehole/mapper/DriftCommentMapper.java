package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.treehole.entry.DriftComment;

import java.util.List;

@Mapper
public interface DriftCommentMapper {
    
    @Insert("INSERT INTO drift_hole_comment(drift_hole_id, user_id, content, create_time) " +
            "VALUES(#{driftHoleId}, #{userId}, #{content}, #{createTime})")
    void insert(DriftComment comment);

    @Select("SELECT * FROM drift_hole_comment WHERE drift_hole_id = #{holeId} ORDER BY create_time ASC")
    List<DriftComment> selectByHoleId(Long holeId);
}
