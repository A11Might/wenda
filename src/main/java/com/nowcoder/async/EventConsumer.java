package com.nowcoder.async;

import com.alibaba.fastjson.JSON;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 胡启航
 * @date 2019/9/15 - 21:07
 */
@Service
public class EventConsumer implements InitializingBean, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    // 事件类型 -> 处理事件的handler
    // 每个handler可以处理很多event类型，每种event也可能需要多个handler处理
    // 通过映射关系，构造map
    // 这样在拿到event的时候可以知道需要哪些handler处理，依次处理即可
    private Map<EventType, List<EventHandler>> config = new HashMap<>();
    private ApplicationContext applicationContext;

    @Autowired
    JedisAdapter jedisAdapter;

    @Override
    public void afterPropertiesSet() throws Exception {
        // 通过当前上下文获取所有实现EventHandler接口的类
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        if (beans != null) {
            // 将所有实现的handler通过其所注册的事件与事件类型关联起来
            // 当有新事件触发时，就可以知道该事件需要哪些handler处理
            for (Map.Entry<String, EventHandler> entry : beans.entrySet()) {
                // 当前handler需要处理的事件类型的列表
                List<EventType> eventTypes = entry.getValue().getSupportEventTypes();

                // 将handler其与事件类型关联起来
                for (EventType type : eventTypes) {
                    if (!config.containsKey(type)) {
                        config.put(type, new ArrayList<>());
                    }
                    config.get(type).add(entry.getValue());
                }
            }
        }

        // 处理事件
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    // 从redis优先队列取出事件
                    String key = RedisKeyUtil.getEventQueueKey();
                    List<String> events = jedisAdapter.brpop(0, key);

                    for (String message : events) {
                        // 当前列表第一个元素是key，跳过(第二个元素才是真正的message)
                        if (message.equals(key)) {
                            continue;
                        }

                        // 将string还原为eventmodel
                        EventModel eventModel = JSON.parseObject(message, EventModel.class);

                        if (!config.containsKey(eventModel.getType())) {
                            logger.error("不能识别的事件");
                            continue;
                        }

                        // 所有需要处理当前事件的handler，依次处理当前事件
                        for (EventHandler hander : config.get(eventModel.getType())) {
                            hander.doHandle(eventModel);
                        }
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
