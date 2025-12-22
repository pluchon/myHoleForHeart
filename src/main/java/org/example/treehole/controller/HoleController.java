package org.example.treehole.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.treehole.Constant;
import org.example.treehole.entry.Hole;
import org.example.treehole.entry.LikeMessage;
import org.example.treehole.entry.LoginAndResisterResult;
import org.example.treehole.enums.loginAndResisterStatus;
import org.example.treehole.service.HoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author pluchon
 * @create 2025-12-20-17:03
 * 作者代码水平一般，难免难看，请见谅
 */
@RestController
@RequestMapping("/hole")
public class HoleController {
    @Autowired
    private HoleService holeService;

    //从session中获取登录的用户Id，如果没有就是0
    @PostMapping("/publish")
    public LoginAndResisterResult publish(@RequestBody Hole hole, HttpSession session){
        //通过之前已经存入好的Session获取用户Id
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        // 校验：未登录用户不允许发布
        if (userIdObj == null) {
            return LoginAndResisterResult.notLogin();
        }
        
        // 校验内容长度
        String content = hole.getContent();
        if (content == null || content.length() < Constant.HOLE_TEXT_MIN_LENGTH) {
            return LoginAndResisterResult.textInSufficient();
        }
        if (content.length() > Constant.HOLE_TEXT_MAX_LENGTH) {
            return LoginAndResisterResult.textOver();
        }

        Long userId = (Long) userIdObj;
        
        //不要设置为setId，这个是数据库自增的
        hole.setUserId(userId);
        
        //判断是否插入成功
        boolean success = holeService.create(hole);
        if (success) {
            LoginAndResisterResult result = LoginAndResisterResult.success();
            result.setErrorMessage("发布成功"); // 复用 success 但修改提示语
            return result;
        } else {
            // 发布失败（可能是内容为空）
            LoginAndResisterResult result = new LoginAndResisterResult();
            result.setStatus(loginAndResisterStatus.ILLEGALCHARACTERFORDIGIT); // 借用一个错误状态，或者新建
            result.setErrorMessage("发布失败，内容不能为空");
            return result;
        }
    }

    //获取整个树洞内容，接收前端页码参数和分类参数
    @RequestMapping("/getHoleContent")
    public List<Hole> getHoleContent(@RequestParam(defaultValue = "1") Integer page, 
                                     @RequestParam(required = false) String category, HttpSession session){
        // 获取当前用户ID，用于判断是否已点赞
        Object userIdObj = session.getAttribute(Constant.USER_ID);
        Long userId = (userIdObj != null) ? (Long) userIdObj : 0L;
        
        return holeService.queryAllContent(page, userId, category);
    }

    @RequestMapping("/like")
    public boolean like(Long id, HttpSession session){
        // 获取当前用户ID
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if (userId == null) {
            return false; // 未登录不能点赞，前端处理
        }
        return holeService.like(id, userId);
    }

    //添加我的树洞这个功能
    @RequestMapping("/myHole")
    public List<Hole> myHole(HttpSession session){
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if(userId == null){
            //没有当前用户就去返回空结果
            return new ArrayList<>();
        }
        // 查看自己的树洞，viewerId也是自己
        return holeService.queryByUserId(userId, userId);
    }

    // 查看指定用户的树洞 (用于私信界面点击头像查看)
    @RequestMapping("/userHole")
    public List<Hole> userHole(Long targetUserId, HttpSession session){
        // 获取当前登录用户Id (查看者)
        Long viewerId = (Long)session.getAttribute(Constant.USER_ID);
        // 如果未登录，viewerId设为0，这样点赞状态都是false
        if(viewerId == null) viewerId = 0L;
        
        return holeService.queryByUserId(targetUserId, viewerId);
    }

    //用户自己删除自己的已发布的树洞
    @RequestMapping("/deleteByUser")
    public boolean deleteByUser(HttpSession session,Long id){
        //获取当前用户Id，通过session
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        //如果未登录删除不了
        if(userId == null){
            return false;
        }
        //执行删除，前端传入该用户选择删除的树洞Id
        return holeService.deleteByUser(id,userId);
    }

    //获取点赞的通知
    @RequestMapping("/getLikeMessage")
    public List<LikeMessage> getLikeMessage(HttpSession session){
        Long userId = (Long)session.getAttribute(Constant.USER_ID);
        if(userId == null){
            //未登录无法获取
            return new ArrayList<>();
        }
        return holeService.getLikeMessage(userId);
    }
}
