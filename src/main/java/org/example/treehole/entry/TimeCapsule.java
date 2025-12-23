package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

/**
 * 时空胶囊实体类
 * 用于存储用户写给未来的信
 */
@Data
public class TimeCapsule {
    private Long id;
    private Long userId;
    private String userNickname; // 冗余存储昵称，方便展示
    private String content;      // 信件内容
    private Date createTime;     // 写信时间
    private Date unlockTime;     // 拆封时间
    private Integer status;      // 状态：0-未拆封，1-已拆封
}
