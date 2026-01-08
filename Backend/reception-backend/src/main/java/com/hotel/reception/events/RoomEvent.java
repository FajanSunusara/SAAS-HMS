package com.hotel.reception.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private String eventType; // ROOM_UPDATED, ROOM_CREATED, ROOM_DELETED
    private Long roomId;
    private String roomNumber;
    private String oldStatus;
    private String newStatus;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
