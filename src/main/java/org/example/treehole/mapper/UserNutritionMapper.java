package org.example.treehole.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.treehole.entry.UserNutrition;

@Mapper
public interface UserNutritionMapper {

    @Select("SELECT user_id as userId, seed_count as seedCount, last_check_in_date as lastCheckInDate, " +
            "continuous_days as continuousDays, total_days as totalDays FROM user_nutrition WHERE user_id = #{userId}")
    UserNutrition findByUserId(Long userId);

    @Insert("INSERT INTO user_nutrition(user_id, seed_count, last_check_in_date, continuous_days, total_days) " +
            "VALUES(#{userId}, #{seedCount}, #{lastCheckInDate}, #{continuousDays}, #{totalDays})")
    void insert(UserNutrition userNutrition);

    @Update("UPDATE user_nutrition SET seed_count = #{seedCount}, last_check_in_date = #{lastCheckInDate}, " +
            "continuous_days = #{continuousDays}, total_days = #{totalDays} WHERE user_id = #{userId}")
    void update(UserNutrition userNutrition);
}
