package org.example.treehole.service;

import org.example.treehole.entry.AllResult;
import org.example.treehole.entry.CheckInLog;
import org.example.treehole.entry.UserNutrition;
import org.example.treehole.enums.loginAndResisterStatus;
import org.example.treehole.mapper.CheckInLogMapper;
import org.example.treehole.mapper.UserNutritionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NutritionService {

    @Autowired
    private UserNutritionMapper nutritionMapper;

    @Autowired
    private CheckInLogMapper checkInLogMapper;

    public UserNutrition getInfo(Long userId) {
        UserNutrition info = nutritionMapper.findByUserId(userId);
        if (info == null) {
            info = new UserNutrition(userId, 0, null, 0, 0);
            nutritionMapper.insert(info);
        }
        return info;
    }

    @Transactional
    public AllResult checkIn(Long userId) {
        UserNutrition info = getInfo(userId);
        LocalDate today = LocalDate.now();
        
        if (today.equals(info.getLastCheckInDate())) {
            return AllResult.checkInAlready();
        }

        LocalDate yesterday = today.minusDays(1);
        int continuousDays = 1;
        if (info.getLastCheckInDate() != null && yesterday.equals(info.getLastCheckInDate())) {
            continuousDays = info.getContinuousDays() + 1;
        }

        int reward = 10; // Base reward
        String message = "签到成功！获得 10 个种子";

        // Bonus logic
        if (continuousDays == 7) {
            reward += 100;
            message = "连续签到7天！额外获得 100 个种子！总计 110 个";
        } else if (continuousDays == 14) {
            reward += 200;
            message = "连续签到14天！额外获得 200 个种子！总计 210 个";
        } else if (continuousDays == 30) {
            reward += 500;
            message = "连续签到30天！额外获得 500 个种子！总计 510 个";
        }

        info.setSeedCount(info.getSeedCount() + reward);
        info.setLastCheckInDate(today);
        info.setContinuousDays(continuousDays);
        info.setTotalDays(info.getTotalDays() + 1);

        nutritionMapper.update(info);

        // Record log
        try {
            CheckInLog log = new CheckInLog();
            log.setUserId(userId);
            log.setCheckInDate(java.sql.Date.valueOf(today));
            checkInLogMapper.insert(log);
        } catch (Exception e) {
            // Ignore duplicate key error if concurrent check-in happens or re-run
            // e.printStackTrace();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("reward", reward);
        data.put("continuousDays", continuousDays);
        data.put("seedCount", info.getSeedCount());

        AllResult result = AllResult.success();
        result.setErrorMessage(message); 
        result.setData(data);
        return result;
    }

    public List<String> getMonthlyCheckInLog(Long userId, String yearMonth) {
        return checkInLogMapper.selectCheckInDatesByMonth(userId, yearMonth);
    }
}
