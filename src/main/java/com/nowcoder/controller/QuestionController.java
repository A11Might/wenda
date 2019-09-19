package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.*;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/18 - 18:36
 */
@Controller
public class QuestionController {
    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);

    @Autowired
    QuestionService questionService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

    @Autowired
    UserService userService;

    @Autowired
    FollowService followService;

    @RequestMapping(path = {"/question/add"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content) {
        try {
            Question question = new Question();
            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            if (hostHolder.getUser() == null) {
                // 未登录，则匿名添加问题
//                question.setUserId(WendaUtil.ANONYMOUS_USERID);
                // 未登录，则跳转到登录页面
                return WendaUtil.getJSONString(999);
            } else {
                question.setUserId(hostHolder.getUser().getId());
            }

            // 添加问题成功，触发事件，给问题添加全文搜索(未做)
            if (questionService.addQuestion(question) > 0) {
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                .setActorId(question.getId())
                .setEntityId(question.getId())
                .setExt("title", question.getTitle())
                .setExt("content", question.getContent()));
                return WendaUtil.getJSONString(0);
            }
        } catch (Exception e) {
            logger.error("增加题目失败" + e.getMessage());
        }

        // 添加问题失败
        return WendaUtil.getJSONString(1, "失败");
    }

    @RequestMapping(path = {"/question/{qid}"}, method = {RequestMethod.GET})
    public String questionDetail(Model model,
                                 @PathVariable("qid") int qid) {
        Question question = questionService.getQuestionById(qid);
        model.addAttribute("question", question);

        // 获取当前问题的所有评论(用于前端显示)
        List<Comment> commentList = commentService.getCommentsByEntity(EntityType.ENTITY_QUESTION, qid);
        // 使用vo整合评论内容和发评论的用户信息，一并发给前端渲染
        List<ViewObject> comments = new ArrayList<>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);
            // 当前用户对，当前评论是点踩还是点赞
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }
            // 当前评论的点赞数
            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));
            // 当前评论的作者
            vo.set("user", userService.getUserById(comment.getUserId()));
            comments.add(vo);
        }
        model.addAttribute("comments", comments);

        // 获取所有当前问题的关注者
        List<Integer> userIds = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        // 使用vo整合所有关注问题的用户信息，一并发给前端渲染
        List<ViewObject> followUsers = new ArrayList<>();
        for (Integer userId : userIds) {
            ViewObject vo = new ViewObject();
            User user = userService.getUserById(userId);
            if (user == null) {
                continue;
            }
            vo.set("name", user.getName());
            vo.set("headUrl", user.getHeadUrl());
            vo.set("id", user.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);

        // 当前用户是否关注当前问题(用于前端显示)
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed",
                    followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }

        return "detail";
    }
}
