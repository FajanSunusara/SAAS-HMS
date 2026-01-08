package com.hotel.reception.config;

import com.hotel.reception.events.RoomEventSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class RedisPubSubConfig {
    
    @Value("${app.redis.pubsub.roomTopic:hotel.room.events}")
    private String roomTopicName;
    
    @Bean
    public ChannelTopic roomTopic() {
        return new ChannelTopic(roomTopicName);
    }
    
    @Bean
    public MessageListenerAdapter roomEventListener(RoomEventSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }
    
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter roomEventListener,
            ChannelTopic roomTopic) {
        
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(roomEventListener, roomTopic);
        return container;
    }
}
