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
public class InvoiceResponse {
    private Long invoiceId;
    private String invoiceNumber;
    
    private BookingResponse booking;
    private GuestResponse guest;
    
    private LocalDate issueDate;
    private LocalDate dueDate;
    
    // Charges breakdown
    private BigDecimal roomCharges;
    private BigDecimal serviceCharges;
    private BigDecimal foodCharges;
    private BigDecimal otherCharges;
    
    private BigDecimal subtotal;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    
    private String status;
    private String notes;
    
    private List<PaymentResponse> payments;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
