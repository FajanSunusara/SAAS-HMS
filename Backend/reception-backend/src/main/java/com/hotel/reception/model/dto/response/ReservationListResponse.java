package com.hotel.reception.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReservationListResponse {
    private Long id;
    private String bookingId;
    private Long guestId;
    private String guestName;
    private String roomNumber;
    private String roomType;
    private String status;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private BigDecimal amount;
    private String paymentStatus;
    private String source;
    private String floor;
    private String specialRequests;
    private LocalDateTime createdAt;
}