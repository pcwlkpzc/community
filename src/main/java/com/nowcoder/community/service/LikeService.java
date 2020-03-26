package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户对实体进行点赞，
     * 在redis中使用set结构，存贮当前实体有哪些用户进行了点赞功能。
     * 同时，在redis中使用String-Value结构，存储被点赞的用户，收到的所有的赞
     * 由于上述两个功能是同步发生的，应该具有事务性，所以我们使用编程式事务，进行控制。
     * @param userId 点赞的用户的id
     * @param entityType 实体的类型
     * @param entityId 实体类对象的id
     * @param entityUserId 被点赞的用户id，即当前被点赞实体的拥有者
     */
    public void like(int userId, int entityType, int entityId, int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);//此处的查询操作放在事务之外

                operations.multi();//开启事务
                if (isMember){//已经点赞
                    operations.opsForSet().remove(entityLikeKey,userId);//取消点赞
                    operations.opsForValue().decrement(userLikeKey);//对被点赞用户的总点赞数减1
                }else {//还没有点过赞
                    operations.opsForSet().add(entityLikeKey,userId);//进行点赞
                    operations.opsForValue().increment(userLikeKey);//对被点赞用户的总点赞数加1
                }
                return operations.exec();//提交事务
            }
        });
    }

    /**
     * 查询某个实体对象的点赞数
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId){
        String entityKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        Long size = redisTemplate.opsForSet().size(entityKey);
        return size;
    }

    /**
     * 查询某个用户对某实体类的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityKey, userId) ? 1 : 0;
    }

    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }
}
