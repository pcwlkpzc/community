package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注和取消关注功能的controller
 */
@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /**
     * 关注功能，不刷新整个页面，使用异步请求
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired//当前方法必须先登录才能执行
    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已关注");
    }

    /**
     * 取消关注功能
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginRequired//当前方法必须先登录才能执行
    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unFollow(int entityType, int entityId){
        User user = hostHolder.getUser();

        followService.unFollow(user.getId(),entityType,entityId);
        return CommunityUtil.getJSONString(0,"已取消关注");
    }

    /**
     * 查询用户关注的列表
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followees/{userId}",method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId , Page page, Model model){
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);

        if (user == null){
            throw new RuntimeException("用户不存在");
        }

        //设置分页
        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int)followService.findFolloweeCount(userId,ENTITY_TYPE_USER));

        List<Map<String, Object>> followees = followService.findFollowees(userId, page.getOffset(), page.getLimit());
        if (followees != null){
            for (Map<String, Object> map : followees) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",followees);

        return "/site/followee";
    }

    /**
     * 查询用户粉丝的列表
     * @param userId
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/followers/{userId}",method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId , Page page, Model model){
        User user = userService.findUserById(userId);
        model.addAttribute("user",user);

        if (user == null){
            throw new RuntimeException("用户不存在");
        }

        //设置分页
        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int)followService.findFollowerCount(ENTITY_TYPE_USER,userId));

        List<Map<String, Object>> followers = followService.findFollowers(userId, page.getOffset(), page.getLimit());
        if (followers != null){
            for (Map<String, Object> map : followers) {
                User u = (User) map.get("user");
                map.put("hasFollowed",hasFollowed(u.getId()));
            }
        }
        model.addAttribute("users",followers);

        return "/site/follower";
    }

    //一个简单的功能复用，查看当前用户是否已经关注其他用户
    private boolean hasFollowed(int userId){
        if (hostHolder.getUser() == null){
            return false;
        }
        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }

}
