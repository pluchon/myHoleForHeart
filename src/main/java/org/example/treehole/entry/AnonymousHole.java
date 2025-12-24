package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

/**
 * 真正匿名树洞实体类
 */
@Data
public class AnonymousHole {
    private Long id;
    private String content;
    private Date createTime;
}
