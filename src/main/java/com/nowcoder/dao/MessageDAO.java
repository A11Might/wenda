package com.nowcoder.dao;

import com.nowcoder.model.Message;
import org.apache.ibatis.annotations.*;


import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/17 - 23:02
 */
@Mapper
public interface MessageDAO {
    String TABLE_NAME = " message ";
    String INSERT_FIELDS = " from_id, to_id, created_date, content, has_read, conversation_id ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    // 向数据库插入一条message
    @Insert({"insert into ", TABLE_NAME, "(",  INSERT_FIELDS,
             ") values(#{fromId},#{toId},#{createdDate},#{content},#{hasRead},#{conversationId})"})
    int addMessage(Message message);

    // 通过conversationId获取两用户之间所有私信
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
             " where conversation_id=#{conversationId} order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationDetail(@Param("conversationId") String conversationId,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

    // 获取某用户发给当前登录用户私信中未读的私信
    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    List<Message> getUnreadConversations(@Param("userId") int userId,
                              @Param("conversationId") String conversationId);

    // 获取私信列表页信息
    // 按时间逆序所有私信后，获取所有与当前用户有关的私信的时间最近的一条，再在按时间逆序排列
    @Select({"select ", INSERT_FIELDS, " , count(id) as id from ( select * from ", TABLE_NAME,
            " where from_id=#{userId} or to_id=#{userId} order by created_date desc) tt group by conversation_id order by created_date desc limit #{offset}, #{limit}"})
    List<Message> getConversationList(@Param("userId") int userId,
                                      @Param("offset") int offset,
                                      @Param("limit") int limit);

    // 获取某用户发给当前登录用户私信中未读的数量(用于前端显示)
    @Select({"select count(id) from ", TABLE_NAME,
            " where has_read=0 and to_id=#{userId} and conversation_id=#{conversationId}"})
    int getConversationUnreadCount(@Param("userId") int userId,
                                   @Param("conversationId") String conversationId);

    // 标记私信已读
    @Update({"update message set has_read=#{hasRead} where id=#{id}"})
    int updateHasRead(@Param("id") int id,
                     @Param("hasRead") int hasRead);
}
