package org.example.treehole.entry;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AIMessage {
    private Integer id;
    private Long userId;
    private String aiType; // general, emotional, guardian, etc.
    private String sender; // user, ai
    private String content;
    private LocalDateTime createTime;
}
