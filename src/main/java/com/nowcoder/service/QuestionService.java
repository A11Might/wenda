package com.nowcoder.service;

import com.nowcoder.dao.QuestionDAO;
import com.nowcoder.model.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/19 - 9:24
 */
@Service
public class QuestionService {
    @Autowired
    SensitiveService sensitiveService;

    @Autowired
    QuestionDAO questionDAO;

    public int addQuestion(Question question) {
        // 过滤标题和内容中的html标签和敏感词
        question.setTitle(HtmlUtils.htmlEscape(question.getTitle()));
        question.setContent(HtmlUtils.htmlEscape(question.getContent()));
        question.setTitle(sensitiveService.filter(question.getTitle()));
        question.setContent(sensitiveService.filter(question.getContent()));

        // 添加问题成功返回问题id
        return questionDAO.addQuestion(question) > 0 ? question.getId() : 0;
    }

    public Question getQuestionById(int id) {
        return questionDAO.selectQuestionById(id);
    }

    public List<Question> getLatestQuestions(int userId, int offset, int limit) {
        return questionDAO.selectLatestQuestions(userId, offset, limit);
    }

    public int updateCommentCount(int id, int count) {
        return questionDAO.updateCommentCount(id, count);
    }
}
