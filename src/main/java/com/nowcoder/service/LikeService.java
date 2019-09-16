package com.nowcoder.service;

import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 胡启航
 * @date 2019/9/16 - 18:24
 */
@Service
public class LikeService {
    @Autowired
    JedisAdapter jedisAdapter;

    // 每个实体有两个集合，分别记录每个点赞和点踩的用户id
    // 点赞某个实体
    public long like(int userId, int entityType, int entityId) {
        // 在实体的点赞集合中，添加当前用户(使用集合自动去重)
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.sadd(likeKey, String.valueOf(userId));

        // 在实体的点踩集合中，删除当前用户
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.srem(disLikeKey, String.valueOf(userId));

        // 获取当前实体点赞数(用于前端显示)
        return jedisAdapter.scard(likeKey);
    }

    // 点踩某个实体
    public long disLike(int userId, int entityType, int entityId) {
        // 在实体的点踩集合中，添加当前用户(使用集合自动去重)
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        jedisAdapter.sadd(disLikeKey, String.valueOf(userId));

        // 在实体的点赞集合中，删除当前用户
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        jedisAdapter.srem(likeKey, String.valueOf(userId));

        // 获取当前实体点赞数(用于前端显示)
        return jedisAdapter.scard(likeKey);
    }

    // 获取实体的点赞数
    public long getLikeCount(int entityType, int entityId) {
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        return jedisAdapter.scard(likeKey);
    }

    // 获取当前登录用户对实体的状态(点赞、点踩或无操作)(用于前端显示)
    public long getLikeStatus(int userId, int entityType, int entityId) {
        // 若在点赞集合中，则返回1
        String likeKey = RedisKeyUtil.getLikeKey(entityType, entityId);
        if (jedisAdapter.sismember(likeKey, String.valueOf(userId))) {
            return 1;
        }
        // 若在点踩集合中，则返回-1
        String disLikeKey = RedisKeyUtil.getDisLikeKey(entityType, entityId);
        return jedisAdapter.sismember(disLikeKey, String.valueOf(userId)) ? -1 : 0;
        // 无操作返回0
    }
}
