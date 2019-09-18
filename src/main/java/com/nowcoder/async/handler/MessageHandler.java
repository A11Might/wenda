package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Message;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/18 - 9:46
 */
@Component
public class MessageHandler implements EventHandler {
    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Override
    public void doHandle(EventModel model) {
        int toId = model.getActorId();
        String conversationId = model.getExt("conversationId");
        List<Message> messageList = messageService.getUnreadConversations(toId, conversationId);
        for (Message message : messageList) {
            messageService.hasReadMessage(message.getId());
        }
    }

    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.HAS_READ);
    }
}
