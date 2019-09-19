package com.nowcoder.dao;

import com.nowcoder.model.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @author 胡启航
 * @date 2019/9/17 - 18:40
 */
@Mapper
public interface CommentDAO {
    String TABLE_NAME = " comment ";
    String INSERT_FIELDS = " user_id, entity_type, entity_id, content, created_date, status ";
    String SELECT_FIELDS = " id, " + INSERT_FIELDS;

    // {}是将一个个逗号隔开的字符串连接起来
    // Param是将形参的值赋给sql语句中的变量(如#{var})(只有一个变量时，不用)
    @Insert({"insert into ", TABLE_NAME, "(", INSERT_FIELDS,
            ") values(#{userId},#{entityType},#{entityId},#{content},#{createdDate},#{status})"})
    int addComment(Comment comment);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME, " where id=#{id}"})
    Comment getCommentById(int id);

    @Select({"select ", SELECT_FIELDS, " from ", TABLE_NAME,
            " where entity_type=#{entityType} and entity_id=#{entityId} order by created_date desc"})
    List<Comment> getCommentsByEntity(@Param("entityType") int entityType,
                                     @Param("entityId") int entityId);

    @Select({"select count(id) from ", TABLE_NAME, " where entity_type=#{entityType} and entity_id=#{entityId}"})
    int getEntityCommentCount(@Param("entityType") int entityType,
                              @Param("entityId") int entityId);

    @Select({"select count(id) from ", TABLE_NAME, " where user_id=#{userId}"})
    int getUserCommentCount(int userId);

    @Update({"update comment set status=#{status} where id=#{id}"})
    int updateStatus(@Param("id") int id,
                     @Param("status") int status);

}