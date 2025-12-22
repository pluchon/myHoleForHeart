package org.example.treehole.entry;

import lombok.Data;

/**
 * @author pluchon
 * @create 2025-12-20-16:08
 * 作者代码水平一般，难免难看，请见谅
 */
@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String nickname;
    //头像图片地址
    private String avatar;
}
