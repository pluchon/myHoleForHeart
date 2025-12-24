package org.example.treehole.entry;

import lombok.Data;

/**
 * Top 10 用户 DTO
 * 包含用户信息、粉丝数以及与当前用户的关注状态
 */
@Data
public class TopUserDTO {
    private Long id;
    private String nickname;
    private String avatar;
    private Integer fansCount;
    
    // 关注状态 (用于前端显示按钮)
    private boolean isFollowing;
    private boolean isMutual;
}
