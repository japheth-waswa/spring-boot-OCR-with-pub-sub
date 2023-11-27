package dev.japhethwaswa.ocrservice.redis.publisher;

import dev.japhethwaswa.ocrservice.redis.config.IMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
public class OcrSuccessPublisher implements IMessagePublisher {
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private ChannelTopic topic;

    public OcrSuccessPublisher(RedisTemplate<String, Object> redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(String message) {
        redisTemplate.convertAndSend(topic.getTopic(),message);
    }
}
