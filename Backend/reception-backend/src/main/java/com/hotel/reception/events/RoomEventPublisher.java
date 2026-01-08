package com.hotel.reception.events;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomEventPublisher {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ChannelTopic roomTopic;
    
    public void publishRoomUpdated(RoomEvent event) {
        log.info("Publishing room event to Redis: Room {} status changed to {}", 
                event.getRoomNumber(), event.getNewStatus());
        
        try {
            redisTemplate.convertAndSend(roomTopic.getTopic(), event);
            log.debug("Room event published successfully to topic: {}", roomTopic.getTopic());
        } catch (Exception e) {
            log.error("Failed to publish room event to Redis", e);
        }
    }
}
