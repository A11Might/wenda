package com.nowcoder.service;

import com.nowcoder.dao.LoginTicketDAO;
import com.nowcoder.dao.UserDAO;
import com.nowcoder.model.LoginTicket;
import com.nowcoder.model.User;
import com.nowcoder.util.WendaUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author 胡启航
 * @date 2019/9/19 - 8:49
 */
@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    UserDAO userDAO;

    @Autowired
    LoginTicketDAO loginTicketDAO;

    public Map<String, Object> login(String username, String password) {
        // 使用map记录登录的错误信息(用于前端显示)
        Map<String , Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }
        User user = userDAO.selectUserByName(username);
        if (user == null) {
            map.put("msg", "用户名不存在");
            return map;
        }

        // 验证用户密码是否正确
        if (!WendaUtil.MD5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("mag", "密码不正确");
            return map;
        }

        // 成功验证登录后，生成t票
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        map.put("userId", user.getId());
        return map;
    }

    public Map<String, Object> register(String username, String password) {
        // 使用map注册登录的错误信息(用于前端显示)
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("msg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("msg", "密码不能为空");
            return map;
        }
        User user = userDAO.selectUserByName(username);
        if (user != null) {
            map.put("msg", "用户名已经被注册");
            return map;
        }

        // 将当前注册用户，写入数据库
        user = new User();
        user.setName(username);
        String headUrl = String.format("http://images.nowcoder.com/head/$dt.png", new Random().nextInt(1000));
        user.setHeadUrl(headUrl);
        // 使用uuid随机生成salt(假设需要5位)
        user.setSalt(UUID.randomUUID().toString().substring(0, 5));
        // 使用salt和md5加密密码
        user.setPassword(WendaUtil.MD5(password + user.getSalt()));
        userDAO.addUser(user);

        // 成功验证登录后，生成t票
        String ticket = addLoginTicket(user.getId());
        map.put("ticket", ticket);
        return map;
    }

    // 给当前登录用户添加t票
    private String addLoginTicket(int userId) {
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(userId);
        // 设置过期时间(3个月)
        Date date = new Date();
        date.setTime(date.getTime() + 1000 * 3600 * 24);
        ticket.setExpired(date);
        ticket.setStatus(0);
        // 设置t票内容(使用uuid随机生成(其中会含有-，替换掉))
        ticket.setTicket(UUID.randomUUID().toString().replaceAll("-", ""));
        loginTicketDAO.addTicket(ticket);
        return ticket.getTicket();
    }

    // getuserbyid
    public User getUserById(int id) {
        return userDAO.selectUserById(id);
    }

    public User getUserByName(String name) {
        return userDAO.selectUserByName(name);
    }

    // 登出就是将t票过期
    public void logout(String ticket) {
        loginTicketDAO.updateStatus(ticket, 1);
    }
}
