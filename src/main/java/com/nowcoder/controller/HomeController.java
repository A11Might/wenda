package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;


/**
 * @author 胡启航
 * @date 2019/9/19 - 9:40
 */
@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    // 首页
    @RequestMapping(path = {"/", "index"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        // 首页，没有用户id使用0(从所有问题中获取最新的问题)
        model.addAttribute("questions", getLatestQuestions(0, 0, 10));
        return "index";
    }

    // 个人主页
    @RequestMapping(path = {"/user/{userId}"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userIndex(Model model,
                            @PathVariable("userId") int userId) {
        model.addAttribute("questions", getLatestQuestions(userId, 0, 10));
        User user = userService.getUserById(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }

    // 拉去最新的问题
    private List<ViewObject> getLatestQuestions(int userId, int offset, int limit) {
        // 使用vo整合问题内容、发问题用户的信息和当前问题关注数，一并发给前端渲染
        List<Question> questionList = questionService.getLatestQuestions(userId, offset, limit);
        List<ViewObject> questions = new ArrayList<>();
        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question", question);
            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION, question.getId()));
            vo.set("user", userService.getUserById(question.getUserId()));
            questions.add(vo);
        }
        return questions;
    }


}
