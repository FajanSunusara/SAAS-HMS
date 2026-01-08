package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String paymentCode;
    private Long invoiceId;
    private String invoiceNumber;
    private Long bookingId;
    private String bookingCode;
    private String guestName;
    
    private String paymentType;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDate paymentDate;
    
    private String transactionId;
    private String cardLastFour;
    private String cardType;
    private String upiId;
    private String bankName;
    private String gateway;
    
    private String receivedBy;
    private String remarks;
    private String status;
    
    private LocalDateTime createdAt;
}
