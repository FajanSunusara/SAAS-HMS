package com.hotel.reception.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    
    private Long invoiceId;
    private Long bookingId;
    private Long guestId;
    
    @NotNull(message = "Payment type is required")
    private String paymentType; // FULL, PARTIAL, ADVANCE, REFUND
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amountPaid;
    
    @NotNull(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, UPI, BANK_TRANSFER
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    // Card details
    private String transactionId;
    private String cardLastFour;
    private String cardType;
    private String cardAuthCode;
    
    // UPI details
    private String upiId;
    
    // Bank transfer
    private String bankName;
    
    // Gateway
    private String gateway;
    private String gatewayTxnId;
    
    // Cash
    private String cashDenomination;
    
    private String receivedBy;
    private String remarks;
}
