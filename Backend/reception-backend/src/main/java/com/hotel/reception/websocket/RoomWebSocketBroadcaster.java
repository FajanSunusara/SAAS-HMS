package com.hotel.reception.websocket;

import com.hotel.reception.events.RoomEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoomWebSocketBroadcaster {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Broadcast room update to all connected WebSocket clients
     */
    public void broadcastRoomUpdate(RoomEvent event) {
        log.info("Broadcasting room update via WebSocket: Room {}", event.getRoomNumber());
        
        try {
            // Broadcast to all clients subscribed to /topic/rooms
            messagingTemplate.convertAndSend("/topic/rooms", event);
            
            // Also broadcast to specific room topic
            messagingTemplate.convertAndSend("/topic/rooms/" + event.getRoomId(), event);
            
            log.debug("Room update broadcasted successfully to WebSocket clients");
        } catch (Exception e) {
            log.error("Failed to broadcast room update via WebSocket", e);
        }
    }
    
    /**
     * Broadcast to specific room only
     */
    public void broadcastToRoom(Long roomId, RoomEvent event) {
        log.debug("Broadcasting to specific room: {}", roomId);
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, event);
    }
}
