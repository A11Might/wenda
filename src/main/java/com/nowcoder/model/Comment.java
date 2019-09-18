package com.nowcoder.model;

import java.util.Date;

/**
 * @author 胡启航
 * @date 2019/9/17 - 18:33
 */
public class Comment {
    // 谁(userId)
    // 对哪个实体(entityType, entityId(问题或者回答))
    // 评论了什么(content)
    // 评论时间(createdDate)
    // 评论状态(status(0 未删除，1 已删除))
    private int id;
    private int userId;
    private int entityType;
    private int entityId;
    private String content;
    private Date createdDate;
    private int status;

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

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
