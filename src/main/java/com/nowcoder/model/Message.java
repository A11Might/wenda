package com.nowcoder.model;

import java.util.Date;

/**
 * @author 胡启航
 * @date 2019/9/17 - 22:48
 */
public class Message {
    // 谁(fromId)
    // 发给谁(toId)
    // 什么时候发的(createdDate)
    // 发了什么(content)
    // 读了吗(hasRead)
    // 用于寻找两个用户的所有私信(conversationId)
    private int id;
    private int fromId;
    private int toId;
    private Date createdDate;
    private String content;
    private int hasRead;
    private String conversationId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getHasRead() {
        return hasRead;
    }

    public void setHasRead(int hasRead) {
        this.hasRead = hasRead;
    }

    // 用于取出两个用户之间的所有私信(固定格式：userId小的在前userId大的在后)
    public String getConversationId() {
        if (fromId < toId) {
            return String.format("%d_%d", fromId, toId);
        } else {
            return String.format("%d_%d", toId, fromId);
        }
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
