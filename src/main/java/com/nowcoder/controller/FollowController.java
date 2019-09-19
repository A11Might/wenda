package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 胡启航
 * @date 2019/9/15 - 9:41
 */
@Controller
public class FollowController {
    @Autowired
    HostHolder hostHolder;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    QuestionService questionService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            // 未登录，则需要登录
            return WendaUtil.getJSONString(999);
        }

        // 当前用户关注目标用户
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        // 发出关注事件，用于异步发送关注私信
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                    .setActorId(hostHolder.getUser().getId())
                    .setEntityType(EntityType.ENTITY_USER)
                    .setEntityId(userId)
                    .setEntityOwnerId(userId));

        // 返回关注人数(用于更新当前用户的关注数)
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId) {
        if (hostHolder.getUser() == null) {
            // 未登录，则需要登录
            return WendaUtil.getJSONString(999);
        }

        // 当前用户取消关注目标用户
        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);

        // 发出取消关注事件，用于异步发送关注私信
        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUser().getId())
                .setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER)
                .setEntityOwnerId(userId));

        // 返回关注人数(用于前端显示当前用户的关注数)
        return WendaUtil.getJSONString(ret ? 0 : 1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER)));
    }

    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            // 未登录，则需要登录
            return WendaUtil.getJSONString(999);
        }

        Question question = questionService.getQuestionById(questionId);
        if (question == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        // 当前用户关注目标问题
        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        // 发出关注事件，用于异步发送关注私信
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                    .setActorId(hostHolder.getUser().getId())
                    .setEntityType(EntityType.ENTITY_QUESTION)
                    .setEntityId(questionId)
                    .setEntityOwnerId(question.getUserId()));

        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUser().getHeadUrl());
        info.put("name", hostHolder.getUser().getName());
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));

        // 返回当前用户信息(用于前端显示当前问题的粉丝信息)
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId) {
        if (hostHolder.getUser() == null) {
            // 未登录，则需要登录
            return WendaUtil.getJSONString(999);
        }

        Question question = questionService.getQuestionById(questionId);
        if (question == null) {
            return WendaUtil.getJSONString(1, "问题不存在");
        }

        // 当前用户取消关注目标问题
        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);

        // 发出取消关注事件，用于异步发送关注私信
        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW)
                .setActorId(hostHolder.getUser().getId())
                .setEntityType(EntityType.ENTITY_QUESTION)
                .setEntityId(questionId)
                .setEntityOwnerId(question.getUserId()));

        // 取消对目标问题的关注，需要当前用户信息？
        Map<String, Object> info = new HashMap<>();
        info.put("id", hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION, questionId));

        // 返回当前用户信息(用于前端显示当前问题的粉丝信息)
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId) {
        // 通过当前页面用户(userid对应用户)的粉丝id列表来获取所有粉丝信息
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0, 10);
        // 若当前用户登录，则判断其是否当关注前页面用户的粉丝(用于前端当前页面用户粉丝页面显示当前登录用户与它们的关系)
        if (hostHolder.getUser() != null) {
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        } else {
            model.addAttribute("followers", getUsersInfo(0, followerIds));
        }
        model.addAttribute("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser", userService.getUserById(userId));

        return "followers";
    }

    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId) {
        // 通过当前页面用户(userid对应用户)的粉丝id列表来获取所有粉丝信息
        List<Integer> followeeIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);
        // 若当前用户登录，则判断其是否当关注前页面用户的关注者(用于前端当前页面用户粉丝页面显示当前登录用户与它们的关系)
        if (hostHolder.getUser() != null) {
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followeeIds));
        } else {
            model.addAttribute("followees", getUsersInfo(0, followeeIds));
        }
        model.addAttribute("followeeCount",followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        model.addAttribute("curUser", userService.getUserById(userId));

        return "followees";
    }

    /**
     * 通过粉丝id列表获取粉丝信息
     * @param localUserId 当前登录的用户
     * @param userIds 当前页面用户的粉丝id列表
     * @return
     */
    private List<ViewObject> getUsersInfo(int localUserId, List<Integer> userIds) {
        List<ViewObject> userInfos = new ArrayList<>();
        for (Integer uid : userIds) {
            User user = userService.getUserById(uid);
            if (user == null) {
                continue;
            }
            ViewObject vo = new ViewObject();
            vo.set("user", user);
            vo.set("commentCount", commentService.getUserCommentCount(uid));
            vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, uid));
            vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER));
            // 判断当前登录用户是否当前页面用户的粉丝，一并存在粉丝信息中(用于前端当前页面用户粉丝页面显示当前登录用户与它们的关系)
            // 0为设定的为登录标识
            if (localUserId != 0) {
                vo.set("followed", followService.isFollower(localUserId, EntityType.ENTITY_USER, uid));
            } else {
                vo.set("followed", false);
            }
            userInfos.add(vo);
        }

        return userInfos;
    }
}