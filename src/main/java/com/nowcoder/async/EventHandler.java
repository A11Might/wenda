package com.nowcoder.async;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/15 - 20:54
 */
public interface EventHandler {
    // 处理当前事件
    void doHandle(EventModel model);

    // 注册(list中为所有需要当前handler处理的事件类型)
    List<EventType> getSupportEventTypes();
}
