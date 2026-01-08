package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long bookingId;
    private String bookingCode;
    
    // Guest info
    private GuestResponse guest;
    private List<GuestResponse> groupMembers;
    
    private String bookingType;
    private String bookingSource;
    
    // Dates
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private Integer nights;
    private RoomPreferenceResponse roomPreferences; 
    private String cancellationReason; 
    // Occupancy
    private Integer adults;
    private Integer children;
    private Integer infants;
    
    // Rooms
    private List<RoomResponse> rooms;
    
    // Status
    private String status;
    private String paymentStatus;
    
    // Pricing
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    
    // Additional
    private String purposeOfVisit;
    private String specialInstructions;
    
    private LocalDateTime createdAt;
    private String createdBy;
}
