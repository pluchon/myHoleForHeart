package org.example.treehole.entry;

import lombok.Data;
import java.util.Date;

@Data
public class Favorite {
    private Long id;
    private Long userId;
    private Long holeId;
    private Date createTime;
}
