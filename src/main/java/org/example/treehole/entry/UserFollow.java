package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

/**
 * 用户关注实体类
 * 对应数据库表 user_follow
 */
@Data
public class UserFollow {
    /** 主键ID */
    private Long id;
    
    /** 关注者ID (发起关注的用户) */
    private Long followerId;
    
    /** 被关注者ID (目标用户) */
    private Long followedId;
    
    /** 是否特别关注 (0:否, 1:是) - 特别关注会置顶 */
    private Integer isSpecial;
    
    /** 创建时间 */
    private Date createTime;
    
    // --- 非数据库字段 (用于前端展示) ---
    
    /** 被关注者昵称 */
    private String followedNickname;
    
    /** 被关注者头像 */
    private String followedAvatar;
}
