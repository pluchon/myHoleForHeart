package org.example.treehole.entry;

import lombok.Data;

import java.util.Date;

/**
 * @author pluchon
 * @create 2025-12-22-15:59
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
public class Message {
    private Long id;//私信主键Id
    private Long senderId;//发送者Id
    private Long receiverId;//接收者Id
    private String content;//发送内容
    private Date createTime;//发送时间
    private Boolean isRead;//是否已读
}
