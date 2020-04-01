package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据实体类型来查询相关的评论
     * @param entityType 实体类对象的类型
     * @param entityId  当前实体类的id
     * @param offset    分页查询的起始页
     * @param limit 分页查询时，每页的限制条目数
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType,int entityId, int offset,int limit);

    /**
     * 根据回复评论的实体类型和实体的id查询评论的数量
     * @param entityType 实体类对象的类型
     * @param entityId 当前实体类的id
     * @return
     */
    int selectCountByEntity(int entityType,int entityId);

    /**
     * 增加新的评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);

    /**
     * 根据评论的id来查询评论
     * @param id
     * @return
     */
    Comment selectCommentById(int id);
}
