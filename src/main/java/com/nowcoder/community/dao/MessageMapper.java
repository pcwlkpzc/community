package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，
     * 每个会话只返回一条最新的私信消息
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectConversation(int userId,int offset, int limit);

    /**
     * 查询当前用户所有的会话数量
     * @param userId
     * @return
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话包含的所有私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId,int offset, int limit);

    /**
     * 查询某个会话包含的私信数量
     * @param conversationId
     * @return
     */
    int selectLettersCount(String conversationId);

    /**
     * 查询未读私信消息的数量
     * 当有conversationId的时候，查询某个私信会话的未读数量
     * 当没有conversationId的时候，查询当前用户所有的私信会话未读消息的数量
     * @param userId
     * @param conversationId
     * @return
     */
    int selectLetterUnreadCount(int userId,String conversationId);

    /**
     * 新增一条私信
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 修改学习的状态
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids, int status);
}
