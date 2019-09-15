package com.nowcoder.async.handler;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.Message;
import com.nowcoder.model.User;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/15 - 11:57
 */
@Component
public class FollowerHandler implements EventHandler {
    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @Override
    public void doHandle(EventModel model) {
        // 触发关注事件，向被关注实体(用户或问题)发送站内信
        Message message = new Message();
        message.setFromId(WendaUtil.SYSTEM_USERID);
        message.setToId(model.getEntityOwnerId());
        message.setCreatedDate(new Date());
        User actorUser = userService.getUser(model.getActorId());

        if (model.getEntityType() == EntityType.ENTITY_QUESTION) {
            message.setContent("用户" + actorUser.getName() + "关注了你的问题，http:/127.0.0.1:8080/question/" + model.getEntityId());
        } else {
            message.setContent("用户" + actorUser.getName() + "关注了你，http:/127.0.0.1:8080/user/" + model.getActorId());
        }

        messageService.addMessage(message);
    }

    // 所有关注事件(关注用户，关注问题...)使用该handler
    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(EventType.FOLLOW);
    }
}
