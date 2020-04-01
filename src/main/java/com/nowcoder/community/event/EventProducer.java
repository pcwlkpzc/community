package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件的生产者
 */
@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 处理事件
     *
     * 将事件转化为Jason数据格式的字符串，然后发送出去
     * @param event
     */
    public void fireEvent(Event event){
        //将事件发送出去
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }
}
