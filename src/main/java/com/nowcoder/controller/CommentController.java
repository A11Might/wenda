package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Question;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * @author 胡启航
 * @date 2019/9/17 - 19:12
 */
@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(CommentController.class);

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content) {
        try {
            // 将评论插入数据库
            Comment comment = new Comment();
            comment.setContent(content);
            if (hostHolder.getUser() != null) {
                comment.setUserId(hostHolder.getUser().getId());
            } else {
                // 当前用户未登录，则匿名
//                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
                // 当前用户未登录，则跳转到登录界面
                return "redirect:/reglogin";
            }
            comment.setCreatedDate(new Date());
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setEntityId(questionId);
            commentService.addComment(comment);

            // 更新问题评论数(用于前端显示)
            // 添加评论和修改评论数应该是一个事务
            // comment.getEntityId()为问题表的id
            int count = commentService.getEntityCommentCount(comment.getEntityType(), comment.getEntityId());
            questionService.updateCommentCount(comment.getEntityId(), count);

            // 触发评论问题事件(向问题作者发站内信)
            // 匿名没有hostholder设置actorid错误，无法生成事件(简单处理只有登录用户才能评论)
            Question question = questionService.getById(questionId);
            eventProducer.fireEvent(new EventModel(EventType.COMMENT)
                    .setActorId(hostHolder.getUser().getId())
                    .setEntityType(EntityType.ENTITY_QUESTION)
                    .setEntityId(questionId)
                    .setEntityOwnerId(question.getUserId()));
        } catch (Exception e) {
            logger.error("增加评论失败" + e.getMessage());
        }
        return "redirect:/question/" + questionId;
    }
}
