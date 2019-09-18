package com.nowcoder.service;

import com.nowcoder.dao.MessageDAO;
import com.nowcoder.model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/17 - 23:24
 */
@Service
public class MessageService {
    @Autowired
    MessageDAO messageDAO;

    @Autowired
    SensitiveService sensitiveService;

    public int addMessage(Message message) {
        // 过滤html标签和敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveService.filter(message.getContent()));
        return messageDAO.addMessage(message);
    }

    // 获取与某用户中间的所有私信
    public List<Message> getConversationDetail(String conversationId, int offset, int limit) {
        return messageDAO.getConversationDetail(conversationId, offset, limit);
    }

    // 获取与某用户中间的所有未读私信
    public List<Message> getUnreadConversations(int userId, String conversationId) {
        return messageDAO.getUnreadConversations(userId, conversationId);
    }

    // 获取当前用户与所有其他用户最新的一条私信(按时间逆序排序)
    public List<Message> getConversationList(int userId, int offset, int limit) {
        return messageDAO.getConversationList(userId, offset, limit);
    }

    // 获取当前用户所有未读私信
    public int getConversationUnreadCount(int userId, String conversationId) {
        return messageDAO.getConversationUnreadCount(userId, conversationId);
    }

    public boolean hasReadMessage(int userId) {
        return messageDAO.updateHasRead(userId, 1) > 0 ? true : false;
    }
}
