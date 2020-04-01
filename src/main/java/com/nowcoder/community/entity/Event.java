package com.nowcoder.community.entity;

import org.apache.kafka.common.protocol.types.Field;

import java.util.HashMap;
import java.util.Map;

/**
 * 对系统消息：点赞，关注，回复
 * 进行事件驱动的设计方式，
 * 我们将事件封装到这个类中
 */
public class Event {

    /**
     * 事件的主题
     */
    private String topic;

    /**
     * 触发该事件的用户id
     */
    private int userId;

    /**
     * 操作实体的类型
     */
    private int entityType;

    /**
     * 实体id
     */
    private int entityId;

    /**
     * 实体所属用户的id
     */
    private int entityUserId;

    /**
     * 将剩余的数据封装到map中，
     * 便于后序程序扩展的设计
     */
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key, value);
        return this;
    }
}
