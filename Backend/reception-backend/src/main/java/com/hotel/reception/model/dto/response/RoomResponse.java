package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponse {
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private Integer floorNumber;
    private BigDecimal baseRate;
    private String status;
    private Integer capacity;
    private String[] features;
    private String description;
    private Boolean smoking;
    private Boolean accessible;
    private Boolean vip;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Current occupancy info
    private String currentGuestName;
    private Long currentBookingId;
    private LocalDateTime lastUpdated;
  
    private BigDecimal roomRate;
}
