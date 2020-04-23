package com.nowcoder.community.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 对评论操作的服务层
 */
@Service
public class DiscussPostService {

    private static Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    //Caffeine核心接口：Cache，LoadingCache，AsyncLoadingCache;
    /**
     * 帖子列表缓存
     */
    private LoadingCache<String,List<DiscussPost>> postListCache;

    /**
     * 帖子总数缓存
     */
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    public void init(){
        //初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {//此方法主要表示缓存中的数据的来源
                        if (key == null || key.length() == 0){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2){
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        //此处可以增加二级缓存：从redis中查询，如果没有，再访问数据库

                        logger.debug("load post list from DB.");
                        //我们此处固定userId和orderMode，这种参数代表当前在查询首页的热门帖子
                        return discussPostMapper.selectDiscussPosts(0,offset,limit,1);
                    }
                });

        //初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds,TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    /**
     * 按照用户的id来查询所有的评论数量，然后进行分页显示
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode
     * @return
     */
    public List<DiscussPost> findDiscussPosts (int userId, int offset, int limit,int orderMode){
        if(userId == 0 && orderMode == 1){//当没有用户登录，并且按照热度进行排序时，直接从缓存中返回数据
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");
        return discussPostMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    /**
     * 查找该用户的所有评论总数
     * @param userId
     * @return
     */
    public int findDiscussPostRows(int userId){
        if (userId == 0){//此时表示，没有用户登录，查询的是首页数据的行数
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB. ");
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
