package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingBillResponse {
    private Long bookingId;
    private String bookingCode;
    private GuestResponse guest;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer nights;
    private Integer adults;
    private Integer children;
    
    // Charges
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private List<AdditionalCharge> additionalCharges;
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    
    private String status;
    private String paymentStatus;
    private LocalDateTime checkedOutAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdditionalCharge {
        private String description;
        private BigDecimal amount;
        private String category; // ROOM_SERVICE, SPA, LAUNDRY, MINI_BAR, etc.
        private Integer quantity;
        private LocalDateTime chargeDate;
    }
}