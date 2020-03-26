package com.nowcoder.community.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.net.UnknownHostException;

/**
 * redis 的配置类
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) throws UnknownHostException {
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        //将连接工厂传入到template中，作为后面的连接方式
        template.setConnectionFactory(factory);

        //设置redis的key参数序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //设置redis的value参数序列化方式，以json数据格式进行转化，因为json的数据格式具有结构性
        template.setValueSerializer(RedisSerializer.json());
        //设置hash的key的序列化方式
        template.setHashKeySerializer(RedisSerializer.string());
        //设置hash的value的序列化方式
        template.setHashValueSerializer(RedisSerializer.json());

        //触发配置文件
        template.afterPropertiesSet();
        return template;
    }
}
