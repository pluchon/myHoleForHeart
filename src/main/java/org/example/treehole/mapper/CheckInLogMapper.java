package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.treehole.entry.CheckInLog;

import java.util.List;

@Mapper
public interface CheckInLogMapper {
    int insert(CheckInLog log);
    
    List<String> selectCheckInDatesByMonth(@Param("userId") Long userId, @Param("monthPrefix") String monthPrefix);
}
