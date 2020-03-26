package com.nowcoder.community.service;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 用户对某个实体类的关注和取关功能
 */
@Service
public class FollowService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 关注功能，
     * 需要将实体类加入到当前用户关注的实体类的容器中，
     * 也需要将用户加入到实体类的粉丝容器中，
     * 所以我们需要使用事务
     *
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void follow(int userId, int entityType, int entityId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);//代表用户关注的实体
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);//代表某个实体类的粉丝

                operations.multi();//开启事务
                operations.opsForZSet().add(followeeKey,entityId,System.currentTimeMillis());//将实体类加入到用户关注的集合中
                operations.opsForZSet().add(followerKey,userId,System.currentTimeMillis());//将用户加入到实体类的粉丝集合中
                return operations.exec();//提交事务
            }
        });
    }

    /**
     * 取消关注
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void unFollow(int userId, int entityType, int entityId){

        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);//代表用户关注的实体
                String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);//代表某个实体类的粉丝

                operations.multi();//开启事务
                operations.opsForZSet().remove(followeeKey,entityId);//将实体类从用户关注的集合中删除
                operations.opsForZSet().remove(followerKey,userId);//将用户从实体类的粉丝集合中删除
                return operations.exec();//提交事务
            }
        });
    }

    /**
     * 查询用户关注特定类型实体的数量
     * @param userId 用户id
     * @param entityType 需要查询的实体类型
     * @return
     */
    public long findFolloweeCount(int userId,int entityType){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Long size = redisTemplate.opsForZSet().zCard(followeeKey);
        return size == null ? 0 : size.intValue();
    }

    /**
     * 查询实体类所拥有的粉丝数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType,int entityId){
        String followerKey = RedisKeyUtil.getFollowerKey(entityType, entityId);
        Long size = redisTemplate.opsForZSet().zCard(followerKey);
        return size == null ? 0 : size.intValue();
    }

    /**
     * 查询当前用户是否已经关注这个实体类
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType,int entityId){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, entityType);
        Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
        return score != null;
    }

    /**
     * 查询用户关注的人的列表
     * 此处由于我们只查询用户，
     * 所以我们直接将ENTITY_TYPE_USER作为实体类型，
     * 不再从外部获取实体类型。
     *
     * 返回的结果里面，需要有每个用户以及关注他们的时间
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String,Object>> findFollowees(int userId, int offset, int limit ){
        String followeeKey = RedisKeyUtil.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);

        if (targetIds == null){
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer targetId : targetIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(targetId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    /**
     * 查询用户的粉丝列表
     *
     * 返回的结果里面，需要有每个粉丝关注此用户的时间
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String,Object>> findFollowers(int userId, int offset, int limit ){
        String followerKey = RedisKeyUtil.getFollowerKey(ENTITY_TYPE_USER,userId);

        //倒序方式插叙id
        //此处的set被redis进行了特殊的实例化，所以此时的set是一个有序集合
        Set<Integer> followerIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);

        if (followerIds == null){
            return null;
        }

        List<Map<String,Object>> list = new ArrayList<>();
        for (Integer followerId : followerIds) {
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(followerId);
            map.put("user",user);
            Double score = redisTemplate.opsForZSet().score(followerKey, followerId);
            map.put("followTime",new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

}
