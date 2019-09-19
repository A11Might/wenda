package com.nowcoder.model;

import java.util.Date;

/**
 * @author 胡启航
 * @date 2019/9/18 - 19:47
 */
public class LoginTicket {
    // 用户(userId)
    // 的t票(ticket)
    // 过期时间(expired)
    // 是否有效(status)
    private int id;
    private int userId;
    private String ticket;
    private Date expired;
    private int status; // 0有效，1无效

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getExpired() {
        return expired;
    }

    public void setExpired(Date expired) {
        this.expired = expired;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTicket() {
        return ticket;
    }

    public void setTicket(String ticket) {
        this.ticket = ticket;
    }
}
