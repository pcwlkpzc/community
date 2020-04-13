package com.nowcoder.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
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

    /**
     * 测试20万个重复数据的独立总数
     */
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for (int i = 1; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 1 ; i < 100000 ; i++){
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }

        long size =  redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    /**
     * 将3组数据合并，再统计合并后的重读数据的独立总数
     */
    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 1; i <= 10000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }

        String redisKey3 = "test:hll:03";
        for (int i = 5001; i <= 15000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }

        String redisKey4 = "test:hll:04";
        for (int i = 10001; i <= 20000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }

        String unionKey = "test:hhl:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }

    /**
     * 统计一组数据的布尔值
     * 按位进行存储
     */
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        //记录，按照每一位进行存储
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,6,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        //查询每一位的值
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        //统计有多少个true
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);
    }

    /**
     * 统计散珠数据的布尔值，
     * 并对这3组数据做OR运算
     */
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3,2,true);
        redisTemplate.opsForValue().setBit(redisKey3,3,true);
        redisTemplate.opsForValue().setBit(redisKey3,4,true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey4,4,true);
        redisTemplate.opsForValue().setBit(redisKey4,5,true);
        redisTemplate.opsForValue().setBit(redisKey4,6,true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                //做OR运算
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        redisKey.getBytes(),redisKey2.getBytes(),redisKey3.getBytes(),redisKey4.getBytes());

                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(obj);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,6));
    }
}
