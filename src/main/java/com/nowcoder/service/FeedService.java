package com.nowcoder.service;

import com.nowcoder.dao.FeedDAO;
import com.nowcoder.model.Feed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/13 - 8:59
 */
@Service
public class FeedService {
    @Autowired
    FeedDAO feedDAO;

    public boolean addFeed(Feed feed) {
        feedDAO.addFeed(feed);
        // 加入feed成功后，feed的id大于0
        return feed.getId() > 0;
    }

    public Feed getById(int id) {
        return feedDAO.getFeedById(id);
    }

    public List<Feed> getUserFeeds(int maxId, List<Integer> userIds, int count) {
        return feedDAO.selectUserFeeds(maxId, userIds, count);
    }
}
