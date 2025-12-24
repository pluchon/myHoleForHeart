package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DriftHole {
    private Long id;
    private Long userId; // 发布者ID（仅后台记录，前端显示匿名）
    private String content;
    private Date createTime;
    private Integer likeCount;
    private Integer commentCount;

    // 非数据库字段
    private Boolean isLiked; // 当前用户是否点赞
    private List<DriftComment> comments; // 评论列表
}
