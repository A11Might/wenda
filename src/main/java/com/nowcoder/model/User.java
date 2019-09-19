package com.nowcoder.model;

import org.apache.ibatis.annotations.Param;

/**
 * @author 胡启航
 * @date 2019/9/18 - 19:33
 */
public class User {
    // 用户姓名(name)
    // 密码(password)
    // 盐(salt)(用于加密密码，防止撞库)
    // 头像(headUrl)
    private int id;
    private String name;
    private String password;
    private String salt;
    private String headUrl;

    public User() {

    }

    public User(String name) {
        this.name = name;
        this.password = "";
        this.salt = "";
        this.headUrl = "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }
}
