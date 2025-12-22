package org.example.treehole.entry;

import lombok.Data;

import java.util.Date;

/**
 * @author pluchon
 * @create 2025-12-20-16:53
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
public class Hole {
    private Long id;//树洞编号
    private Long userId;//这里如果是未登录，就存入0
    private String content;//发布的内容
    private String category;//内容的类别
    private Integer likeCount;//点赞数，默认是0
    private Integer commentCount;//评论数
    private Date createTime;//创建时间

    // 以下字段用于前端展示，非数据库字段
    private String userNickname;
    private String userAvatar;
    private Boolean isLiked; // 当前用户是否已点赞
}