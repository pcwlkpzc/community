package com.nowcoder.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka(){
        kafkaProducer.sendMessage("test","你好");
        kafkaProducer.sendMessage("test","在么");
        kafkaProducer.sendMessage("test","世界");

        try {
            Thread.sleep(1000 * 10);//阻塞10秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}

@Component
class KafkaProducer{

    /**
     * KafkaTemplate自动被spring管理，所以可以直接注入
     */
    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 使用KafkaTemplates来调用此方法进行发送消息
     * @param topic 消息的主题
     * @param data 消息的内容
     */
    public void sendMessage(String topic, String data){
        kafkaTemplate.send(topic,data);
    }
}

@Component
class KafkaConsumer{

    @KafkaListener(topics = {"test"})
    public void handleMessage(ConsumerRecord record){
        System.out.println(record.value());
    }

}

