package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

@Data
public class CheckInLog {
    private Long id;
    private Long userId;
    private Date checkInDate;
    private Date createTime;
}
