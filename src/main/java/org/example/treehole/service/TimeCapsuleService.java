package org.example.treehole.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.example.treehole.entry.TimeCapsule;
import org.example.treehole.mapper.TimeCapsuleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class TimeCapsuleService {

    @Autowired
    private TimeCapsuleMapper timeCapsuleMapper;

    // 埋下胶囊
    public boolean buryCapsule(TimeCapsule capsule) {
        // 使用 Hutool 的 StrUtil 检查内容是否为空
        if (StrUtil.isBlank(capsule.getContent()) || capsule.getUnlockTime() == null) {
            return false;
        }
        // 使用 Hutool 的 DateUtil 获取当前时间并比较，拆封时间必须晚于当前时间
        if (capsule.getUnlockTime().before(DateUtil.date())) {
            return false;
        }
        
        // 使用 Hutool 的 DateUtil 获取当前时间作为创建时间
        capsule.setCreateTime(DateUtil.date());
        capsule.setStatus(0); // 0 表示未拆封
        timeCapsuleMapper.insert(capsule);
        return true;
    }

    // 获取我的所有胶囊列表
    public List<TimeCapsule> getMyCapsules(Long userId) {
        return timeCapsuleMapper.selectByUserId(userId);
    }
}
