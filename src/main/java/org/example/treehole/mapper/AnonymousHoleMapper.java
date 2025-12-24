package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.treehole.entry.AnonymousHole;

@Mapper
public interface AnonymousHoleMapper {
    /**
     * 随机获取一条匿名树洞
     */
    @Select("SELECT * FROM anonymous_hole ORDER BY RAND() LIMIT 1")
    AnonymousHole getRandomHole();

    /**
     * 插入一条匿名树洞
     */
    @Insert("INSERT INTO anonymous_hole(content) VALUES(#{content})")
    void insert(AnonymousHole hole);
}
