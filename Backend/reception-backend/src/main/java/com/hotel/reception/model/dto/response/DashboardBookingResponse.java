package com.hotel.reception.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardBookingResponse {

    private Long bookingId;
    private String bookingCode;

    private String guestName;
    private String roomNumber;

    private String status;
    private BigDecimal balance;
    
    
    
}
