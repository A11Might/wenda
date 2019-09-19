package com.nowcoder.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.model.User;
import redis.clients.jedis.*;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/16 - 14:22
 */
public class RedisTest {
    public static void print(int index, Object obj) {
        System.out.println(String.format("%d, %s", index, obj.toString()));
    }

    public static void mainx(String[] args) {
        // 默认不填，连接6379本地端口
        // 等于select 9选择第9个数据库
        Jedis jedis = new Jedis("redis://localhost:6379/9");
//        jedis.flushAll(); // 删除所有数据库
        jedis.flushDB(); // 删除当前数据库

        // kv：单一数值，验证码，pv，缓存
        jedis.set("hello", "world"); // set key value
        print(1, jedis.get("hello")); // get key
        jedis.rename("hello", "newhello"); // 给key重命名
        print(1, jedis.get("newhello"));
        jedis.setex("hello2", 15, "world"); // set带过期时间，过期自动删除元素
        jedis.set("pv", "100");
        jedis.incr("pv"); // + 1
        print(2, jedis.get("pv"));
        jedis.incrBy("pv", 5); // + offset
        print(2, jedis.get("pv"));
        jedis.decrBy("pv", 2); // - offset
        print(2, jedis.get("pv"));
        print(3, jedis.keys("*")); // keys * 获取所有key

        // list：双向列表，适用于最新列表，关注列表
        String listName = "list";
        jedis.del(listName); // 删除key
        for (int i = 0; i < 10; i++) {
            jedis.lpush(listName, "a" + String.valueOf(i)); // 从左边插入
        }
        print(4, jedis.lrange(listName, 0, 12)); // index范围取值
        print(4, jedis.lrange(listName, 0, 3));
        print(4, jedis.lrange(listName,2, 6));
        print(5, jedis.llen(listName)); // list长度
        print(6, jedis.lpop(listName)); // 从左边弹出
        print(7, jedis.llen(listName));
        print(8, jedis.lindex(listName, 3)); // 取index位置的值
        print(9, jedis.linsert(listName, BinaryClient.LIST_POSITION.AFTER, "a4", "xxx")); // 插入值
        print(9, jedis.linsert(listName, BinaryClient.LIST_POSITION.BEFORE, "a4", "bbb"));
        print(10, jedis.lrange(listName, 0, 12));

        // hash：对象属性，不定长属性数
        String userKey = "userxx";
        jedis.hset(userKey, "name", "jim"); // set
        jedis.hset(userKey, "age", "12");
        jedis.hset(userKey, "phone", "12312312312");
        print(11, jedis.hget(userKey, "name")); // get
        print(12, jedis.hgetAll(userKey));
        jedis.hdel(userKey, "phone"); // 删除hashmap中的键值对
        print(13, jedis.hgetAll(userKey)); // getall
        print(14, jedis.hexists(userKey, "email")); // containsKey
        print(15, jedis.hexists(userKey, "age"));
        print(16, jedis.hkeys(userKey)); // keySet
        print(17, jedis.hvals(userKey)); // values
        jedis.hsetnx(userKey, "school", "xd"); // 如果不存在就插入，存在就忽略
        jedis.hsetnx(userKey, "name", "hqh");
        print(18, jedis.hgetAll(userKey));

        // set：适用于无顺序的集合，点赞点踩，抽奖，已读，共同好友
        String likeKey1 = "commentLike1";
        String likeKey2 = "commentLike2";
        for (int i = 0; i < 10; i++) {
            jedis.sadd(likeKey1, String.valueOf(i)); // add
            jedis.sadd(likeKey2, String.valueOf(i * i));
        }
        print(19, jedis.smembers(likeKey1)); // getall
        print(20, jedis.smembers(likeKey2));
        print(21, jedis.sunion(likeKey1, likeKey2)); // 并集
        print(22, jedis.sdiff(likeKey1, likeKey2)); // 不同
        print(23, jedis.sinter(likeKey1, likeKey2)); // 交集
        print(24, jedis.sismember(likeKey1, "12")); // contains
        print(25, jedis.sismember(likeKey2, "16"));
        jedis.srem(likeKey1, "5"); // remove
        print(26, jedis.smembers(likeKey1));
        jedis.smove(likeKey2, likeKey1, "25"); // 将likekey2中的25移动到likekey1中
        print(27, jedis.smembers(likeKey1));
        print(28, jedis.scard(likeKey1)); // size
        print(29, jedis.srandmember(likeKey1, 2)); // 随机取count个元素

        // zset 排行榜，优先队列
        String rankKey = "rankKey";
        jedis.zadd(rankKey, 15, "jim"); // add
        jedis.zadd(rankKey, 60, "ben");
        jedis.zadd(rankKey, 80, "lee");
        jedis.zadd(rankKey, 50, "lucy");
        jedis.zadd(rankKey, 90, "hmm");
        print(30, jedis.zcard(rankKey)); // size
        print(31, jedis.zcount(rankKey, 61, 100)); // 区间中元素数
        print(32, jedis.zscore(rankKey, "lucy")); // 当前元素score
        jedis.zincrby(rankKey, 2, "lucy"); // score + 2
        print(33, jedis.zscore(rankKey, "lucy"));
        jedis.zincrby(rankKey, 2, "luc"); // 若+ score时当前元素不存在，则添加当前元素
        print(34, jedis.zscore(rankKey, "luc"));
        print(35, jedis.zrange(rankKey, 0, 100));
        print(36, jedis.zrange(rankKey, 0, 2)); // index范围取值，默认从低到高排序
        print(36, jedis.zrevrange(rankKey, 0, 2)); // index范围取值，从高到低排序
        for (Tuple tuple : jedis.zrangeByScoreWithScores(rankKey, "60", "100")) { // 遍历zset
            print(37, tuple.getElement() + ":" + String.valueOf(tuple.getScore()));
        }
        print(38, jedis.zrank(rankKey, "ben")); // 获取当前元素排名, 默认从低到高排序
        print(39, jedis.zrevrank(rankKey, "ben")); // 获取当前元素排名, 从高到低排序

        String setKey = "zset";
        jedis.zadd(setKey, 1, "a");
        jedis.zadd(setKey, 1, "b");
        jedis.zadd(setKey, 1, "c");
        jedis.zadd(setKey, 1, "d");
        jedis.zadd(setKey, 1, "e");
        jedis.zadd(setKey, 1, "f");
        print(40, jedis.zlexcount(setKey, "-", "+")); // 按字典序排列，"-"代表-无穷，"+"代表+无穷
        print(41, jedis.zlexcount(setKey, "[b", "[d")); // [代表取到
        print(42, jedis.zlexcount(setKey, "(b", "[d")); // (代表取不到
        jedis.zrem(setKey, "b"); // remove
        print(43, jedis.zrange(setKey, 0, 10));
        jedis.zremrangeByLex(setKey, "(c", "+"); // 按字典序取值
        print(44, jedis.zrange(setKey, 0, 10));

        // pool
        JedisPool pool = new JedisPool(); // 默认有8个连接
        for (int i = 0; i < 100; i++) {
            Jedis j = pool.getResource();
            print(45, j.get("pv"));
            j.close(); // 用完需要还回去，不然就一直占着
        }

        // 缓存
        User user = new User();
        user.setName("xx");
        user.setPassword("ppp");
        user.setHeadUrl("a.png");
        user.setSalt("salt");
        user.setId(1);
        print(46, JSONObject.toJSONString(user)); // 将user对象通过json序列化
        jedis.set("user1", JSONObject.toJSONString(user)); // 将对象缓存

        String value = jedis.get("user1");
        User user2 = JSON.parseObject(value, User.class); // 反序列化
        print(47, user2);

        // redis事务
        try {
            Transaction tx = jedis.multi();
            tx.zadd("qq", 2, "1");
            tx.zadd("qq2", 3, "2");
            List<Object> objs = tx.exec();
            tx.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }
}
