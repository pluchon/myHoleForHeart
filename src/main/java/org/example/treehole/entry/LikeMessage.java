package org.example.treehole.entry;

import lombok.Data;

/**
 * @author pluchon
 * @create 2025-12-22-18:22
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
public class LikeMessage {
    private Long userId;//点赞人的ID
    private String nickname;//点赞人的昵称
    private String avatar;//点赞人的头像
    private String holeContent;//被点赞的树洞的内容
}
