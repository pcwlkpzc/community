package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子DiscussPost类的持久层操作接口
 */
@Mapper
public interface DiscussPostMapper {

    /**
     * 这个查询的sql语句会，动态的选择是否拼接userId参数
     * 当在个人主页时，根据userId查询用户自己的所有帖子
     * 当在首页时，我们不用传入userId，sql语句就不会拼接userId参数
     * 考虑到后期的分页功能，所以我们加上offset和limit
     * @param userId 用户id
     * @param offset 起始id
     * @param limit 当前页中包含几条数据
     * @param orderMode 排序的模式，当为0时，按照默认的时间排序，为1时，按照分数排序
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode);

    /**
     * 查询所有的评论的条数
     * 注解@Param用于给参数取别名
     * 如果只有一个参数，并且需要选择性的动态拼接（在<if>标签中使用），则必须加别名
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 发表新的帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost (DiscussPost discussPost);

    /**
     * 根据主键id查询帖子
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 跟新帖子评论的数量
     * @param id
     * @param commentCount
     * @return
     */
    int updateCommentCount(int id,int commentCount);

    /**
     * 修改帖子的类型（比如置顶，或者普通帖子）
     * @param id
     * @param type
     * @return
     */
    int updateType(int id, int type);

    /**
     * 修改帖子的状态（加精，或者删除）
     * @param id
     * @param status
     * @return
     */
    int updateStatus(int id, int status);

    /**
     * 更改帖子的分数
     * @param id
     * @param score
     * @return
     */
    int updateScore(int id, double score);
}
