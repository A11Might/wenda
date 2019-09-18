package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/17 - 23:43
 */
@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @Autowired
    HostHolder hostHolder;

    @Autowired
    MessageService messageService;

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody
    public String addMessage(@RequestParam("toName") String toName,
                             @RequestParam("content") String content) {
        try {
            // 当前用户未登录，跳转到登录界面
            // 若是返回模板，直接重定向；否则返回999
            if (hostHolder.getUser() == null) {
                return WendaUtil.getJSONString(999, "未登录");
            }

            // 判断当前用户是否存在
            User toUser = userService.selectByName(toName);
            if (toUser == null) {
                return WendaUtil.getJSONString(1, "用户不存在");
            }

            // 将消息插入数据库
            Message message = new Message();
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(toUser.getId());
            message.setCreatedDate(new Date());
            message.setContent(content);
            messageService.addMessage(message);
            // 成功返回0
            return WendaUtil.getJSONString(0);
        } catch (Exception e) {
            logger.error("发送消息失败" + e.getMessage());
            return WendaUtil.getJSONString(1, "发信失败");
        }
    }

    @RequestMapping(path = {"/msg/list"}, method = {RequestMethod.GET})
    public String getConversationList(Model model) {
        // 当前用户未登录，跳转到登录界面
        if (hostHolder.getUser() == null) {
            return "redirect:/reglogin";
        }
        // 使用vo整合消息内容、发消息用户的信息和未读数量，一并发给前端渲染
        int localUserId = hostHolder.getUser().getId();
        List<Message> conversationList = messageService.getConversationList(localUserId, 0, 10);
        List<ViewObject> conversations = new ArrayList<>();
        for (Message message : conversationList) {
            ViewObject vo = new ViewObject();
            vo.set("message", message);
            // 获取的是所有与当前登录用户有关的信息(可能是发送者或是接收者)
            // 在自己的私信列表中需要显示对方用户信息，所以如下操作
            int targetId = message.getFromId() == localUserId ? message.getToId() : message.getFromId();
            vo.set("user", userService.getUser(targetId));
            vo.set("unread", messageService.getConversationUnreadCount(localUserId, message.getConversationId()));
            conversations.add(vo);
        }
        model.addAttribute("conversations", conversations);
        return "letter";
    }

    @RequestMapping(path = {"/msg/detail"}, method = {RequestMethod.GET})
    public String getConversationDetail(Model model,
                                        @RequestParam("conversationId") String conversationId) {
        try {
            // 使用vo整合消息内容、发消息用户的信息，一并发给前端渲染
            List<Message> messageList = messageService.getConversationDetail(conversationId, 0, 10);
            List<ViewObject> messages = new ArrayList<>();
            for (Message message : messageList) {
                ViewObject vo = new ViewObject();
                vo.set("message", message);
                vo.set("user", userService.getUser(message.getFromId()));
                messages.add(vo);
            }
            model.addAttribute("messages", messages);

            // 触发事件，将所有未读私信标记为已读
            eventProducer.fireEvent(new EventModel(EventType.HAS_READ)
            .setActorId(hostHolder.getUser().getId())
            .setExt("conversationId", conversationId));
        } catch (Exception e) {
            logger.error("获取详情页失败" + e.getMessage());
        }
        return "letterDetail";
    }
}
