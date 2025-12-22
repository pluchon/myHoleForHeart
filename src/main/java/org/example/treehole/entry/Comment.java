package org.example.treehole.entry;

import lombok.Data;

import java.util.Date;

/**
 * @author pluchon
 * @create 2025-12-20-18:30
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
public class Comment {
    private Long id;
    private Long holeId;
    private Long userId;
    private String content;
    private Date createTime;
    private Integer likeCount; // 点赞数，默认0
    
    // 非数据库字段
    private Boolean isLiked; // 当前用户是否已点赞
    private String userNickname;
    private String userAvatar;
}
