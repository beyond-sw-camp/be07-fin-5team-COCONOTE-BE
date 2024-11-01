package com.example.coconote.config;

import com.example.coconote.api.sse.NotificationMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

//    @Value("${spring.data.redis.host}")
    @Value("${REDIS_HOST}")
    private String redisHost;

//    @Value("${spring.data.redis.port}")
    @Value("${REDIS_PORT}")
    private int redisPort;

    @Bean
    @Primary
    public RedisConnectionFactory defaultRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);  // 환경 변수에서 Redis 호스트 설정
        config.setPort(redisPort);  // 환경 변수에서 Redis 포트 설정
        config.setDatabase(0);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory defaultRedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(defaultRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisConnectionFactory notificationRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);  // 환경 변수에서 Redis 호스트 설정
        config.setPort(redisPort);  // 환경 변수에서 Redis 포트 설정
        config.setDatabase(1);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, String> notificationRedisTemplate(RedisConnectionFactory notificationRedisConnectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(notificationRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisConnectionFactory sectionRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);  // 환경 변수에서 Redis 호스트 설정
        config.setPort(redisPort);  // 환경 변수에서 Redis 포트 설정
        config.setDatabase(2);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> sectionRedisTemplate(RedisConnectionFactory sectionRedisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(sectionRedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory notificationRedisConnectionFactory,
            MessageListenerAdapter messageListenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(notificationRedisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("notification-channel"));
        return container;
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(NotificationMessageListener notificationMessageListener) {
        return new MessageListenerAdapter(notificationMessageListener, "onMessage");
    }
}
