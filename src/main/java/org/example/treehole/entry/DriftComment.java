package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

@Data
public class DriftComment {
    private Long id;
    private Long driftHoleId;
    private Long userId; // 评论者ID
    private String content;
    private Date createTime;
}
