package dev.japhethwaswa.ocrservice.redis.config;

import dev.japhethwaswa.ocrservice.OcrService;
import dev.japhethwaswa.ocrservice.redis.publisher.OcrSuccessPublisher;
import dev.japhethwaswa.ocrservice.redis.subscriber.OcrProcessorSubscriber;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisLettuceConfiguration {

    @Bean
     LettuceConnectionFactory lettuceConnectionFactory(){
        RedisStandaloneConfiguration configuration  = new RedisStandaloneConfiguration();
        configuration.setHostName(OcrService.dotenv.get("REDIS_HOST"));
        configuration.setPassword(OcrService.dotenv.get("REDIS_PASSWORD"));
        configuration.setPort(OcrService.dotenv.get("REDIS_PORT") != null ? Integer.parseInt(OcrService.dotenv.get("REDIS_PORT")) : 6379);
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
  RedisTemplate<String,Object> redisTemplate(){
        RedisTemplate<String,Object> redisTemplate=  new RedisTemplate<>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory());
        redisTemplate.setValueSerializer(new GenericToStringSerializer<Object>(Object.class));
        return redisTemplate;
    }

    //init listeners
    @Bean
    RedisMessageListenerContainer container(){
        final RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
        redisMessageListenerContainer.setConnectionFactory(lettuceConnectionFactory());
        redisMessageListenerContainer.addMessageListener(new MessageListenerAdapter(new OcrProcessorSubscriber()),new ChannelTopic("OCR-TOPIC"));
        return redisMessageListenerContainer;
    }
    //end listeners


    //start publisher
    @Bean
    ChannelTopic topic(){
        return new ChannelTopic("OCR-TOPIC");
    }

    @Bean
    IMessagePublisher messagePublisher(){
        return new OcrSuccessPublisher(redisTemplate(),topic());
    }
    //end publisher

}
