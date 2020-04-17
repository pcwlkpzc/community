package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.swing.text.html.HTML;
import java.util.List;

/**
 * 对评论操作的服务层
 */
@Service
public class DiscussPostService {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 按照用户的id来查询所有的评论数量，然后进行分页显示
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    public List<DiscussPost> findDiscussPosts (int userId, int offset, int limit,int orderMode){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    /**
     * 查找该用户的所有评论总数
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    /**
     * 增加一条评论
     * @param post
     * @return
     */
    public int addDiscussPost(DiscussPost post){
        if (post == null){
            throw new IllegalArgumentException("参数不能为空");
        }

        //转义字符，避免用户在title和comment中输入了HTML相关的标签
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感字符串
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }

    /**
     * 根据帖子的主键来查询帖子内容
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 更新帖子评论的数量
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id,commentCount);
    }

    /**
     * 修改帖子的类型
     * @param id
     * @param type 置顶、普通
     * @return
     */
    public int updateType(int id,int type){
        return discussPostMapper.updateType(id, type);
    }

    /**
     * 修改帖子的状态
     * @param id
     * @param status 加精、删除
     * @return
     */
    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    /**
     * 修改帖子的分数
     * @param id
     * @param score
     * @return
     */
    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }
}
