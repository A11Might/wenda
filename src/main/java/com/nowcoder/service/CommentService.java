package com.nowcoder.service;

import com.nowcoder.dao.CommentDAO;
import com.nowcoder.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/17 - 19:02
 */
@Service
public class CommentService {
    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    CommentDAO commentDAO;

    public int addComment(Comment comment) {
        // 过滤评论中的html标签和敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveService.filter(comment.getContent()));
        return commentDAO.addComment(comment);
    }

    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }

    public List<Comment> getCommentsByEntity(int entityType, int entityId) {
        return commentDAO.getCommentsByEntity(entityType, entityId);
    }

    public int getEntityCommentCount(int entityType, int entityId) {
        return commentDAO.getEntityCommentCount(entityType, entityId);
    }

    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }

    // 删除评论就是将评论状态置为1(数据库中不删除信息)
    public boolean deleteComment(int id) {
        return commentDAO.updateStatus(id, 1) > 0;
    }
}
