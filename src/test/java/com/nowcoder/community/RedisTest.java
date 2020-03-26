package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate ;

    @Test
    public void testString(){
        String redisKey = "test:count";
        int redisValue = 1;

        redisTemplate.opsForValue().set(redisKey,redisValue);
        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }

    @Test
    public void testHashes(){
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey,"id",1);
        redisTemplate.opsForHash().put(redisKey,"username","zhangsan");

        System.out.println(redisTemplate.opsForHash().get(redisKey,"id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey,"username"));
    }

    @Test
    public void testList(){
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey,101);
        redisTemplate.opsForList().leftPush(redisKey,102);
        redisTemplate.opsForList().leftPush(redisKey,103);

        System.out.println(redisTemplate.opsForList().index(redisKey,0));
        System.out.println(redisTemplate.opsForList().range(redisKey,0,2));
        System.out.println(redisTemplate.opsForList().size(redisKey));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    @Test
    public void testSet(){
        String redisKey = "test:teacher";

        redisTemplate.opsForSet().add(redisKey,"张飞","关羽","诸葛亮","刘备");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));//随机弹出
        System.out.println(redisTemplate.opsForSet().size(redisKey));
    }

    @Test
    public void testSortedSet(){
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey,"唐僧",80);
        redisTemplate.opsForZSet().add(redisKey,"悟空",90);
        redisTemplate.opsForZSet().add(redisKey,"悟空",100);
        redisTemplate.opsForZSet().add(redisKey,"八戒",60);
        redisTemplate.opsForZSet().add(redisKey,"沙僧",70);
        redisTemplate.opsForZSet().add(redisKey,"白龙马",75);

        System.out.println(redisTemplate.opsForZSet().range(redisKey,0,2));//从小到大排序
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey,0,2));//从大到小排序
        System.out.println(redisTemplate.opsForZSet().rank(redisKey,"悟空"));//从小到大排序
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey,"悟空"));//从大到小排序
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));//统计有几个value
        System.out.println(redisTemplate.opsForZSet().score(redisKey,"悟空"));//获取悟空的分数
    }

    @Test
    public void testKeys(){
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:teacher",10, TimeUnit.SECONDS);
    }

    //多次访问同一个key的时候，可以将其绑定在一起，直接访问此对象即可
    @Test
    public void testBoundOperations(){
        String redisKey = "test";
        BoundValueOperations operations = redisTemplate.boundValueOps(redisKey);

        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();

        System.out.println(operations.get());
    }

    /**
     * 编程式事务
     * 在redis的事务中，所有的操作都是redis先存放在队列中，并不会立即执行，
     * 当事务提交的时候，所有的命令一起执行操作，
     * 所以在redis的事务中，查询操作一般不会放在事务中。
     */
    @Test
    public void testTx (){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";

                operations.multi();//开启事务
                BoundListOperations boundListOperations = operations.boundListOps(redisKey);
                boundListOperations.leftPush("zhangsan");
                boundListOperations.leftPush("lisi");
                boundListOperations.leftPush("wangwu");
                System.out.println(boundListOperations.range(0,2));
                return operations.exec();//提交事务
            }
        });
        System.out.println(obj);
    }
}
