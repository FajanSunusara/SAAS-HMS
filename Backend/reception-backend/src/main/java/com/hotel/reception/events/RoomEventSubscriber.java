package com.hotel.reception.events;

import com.hotel.reception.websocket.RoomWebSocketBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomEventSubscriber implements MessageListener {
    
    private final RoomWebSocketBroadcaster webSocketBroadcaster;
    private final GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
    
    @Override
    public void onMessage(Message message, byte[] pattern) {
        log.debug("Received room event from Redis Pub/Sub");
        
        try {
            RoomEvent event = (RoomEvent) serializer.deserialize(message.getBody());
            
            log.info("Processing room event: Room {} status changed from {} to {}", 
                    event.getRoomNumber(), event.getOldStatus(), event.getNewStatus());
            
            // Forward to WebSocket clients
            webSocketBroadcaster.broadcastRoomUpdate(event);
            
        } catch (Exception e) {
            log.error("Error processing room event from Redis", e);
        }
    }
}
