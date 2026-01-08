package com.hotel.reception.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    
    @NotBlank(message = "Room number is required")
    private String roomNumber;
    
    @NotBlank(message = "Room type is required")
    private String roomType;
    
    private Integer floorNumber;
    
    @NotNull(message = "Base rate is required")
    @Positive(message = "Base rate must be positive")
    private BigDecimal baseRate;
    
    private String status;
    private Integer capacity;
    private String[] features;
    private String description;
    private Boolean smoking;
    private Boolean accessible;
    private Boolean vip;
}
