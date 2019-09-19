package com.nowcoder.model;

import org.springframework.stereotype.Component;

/**
 * @author 胡启航
 * @date 2019/9/18 - 19:50
 */
@Component
public class HostHolder {
    // 当前用户，每个线程有一个拷贝份
    private static ThreadLocal<User> users = new ThreadLocal<>();

    public User getUser() {
        return users.get();
    }

    public void setUser(User user) {
        users.set(user);
    }

    public void clear() {
        users.remove();
    }
}
