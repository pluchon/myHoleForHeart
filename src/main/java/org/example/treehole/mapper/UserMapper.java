package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.treehole.entry.User;

/**
 * @author pluchon
 * @create 2025-12-20-16:10
 * 作者代码水平一般，难免难看，请见谅
 */
@Mapper
public interface UserMapper {
    //查重和校验
    @Select("select * from user where username = #{username}")
    User findByUsername(String username);

    //发布树洞
    @Insert("insert into user(username,password,nickname) values(#{username},#{password},#{nickname})")
    void insert(User user);

    //根据Id获取用户信息
    @Select("select * from user where id = #{id}")
    User queryById(Long id);

    //添加更新用户信息语句
    @Update("update user set nickname = #{nickname},avatar=#{avatar} where id = #{id}")
    void update(User user);

    // 修改密码
    @Update("update user set password = #{password} where id = #{id}")
    void updatePassword(Long id, String password);
}
