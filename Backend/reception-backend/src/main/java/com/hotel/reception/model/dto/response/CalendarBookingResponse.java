package com.hotel.reception.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class CalendarBookingResponse {
    private Long bookingId;
    private String bookingCode;
    private Long roomId;
    private String roomNumber;
    private Long guestId;
    private String guestName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String status;
    private Integer adults;
    private Integer children;
    private String source;
    private String paymentStatus;
}