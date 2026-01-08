package com.hotel.reception.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;
    
    @Column(name = "payment_code", unique = true, nullable = false, length = 50)
    private String paymentCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;
    
    @Column(name = "payment_type", length = 20)
    private String paymentType = "FULL";
    
    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;
    
    @Column(name = "transaction_id", length = 100)
    private String transactionId;
    
    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;
    
    @Column(name = "card_type", length = 20)
    private String cardType;
    
    @Column(name = "card_auth_code", length = 50)
    private String cardAuthCode;
    
    @Column(name = "upi_id", length = 100)
    private String upiId;
    
    @Column(name = "bank_name", length = 100)
    private String bankName;
    
    @Column(name = "gateway", length = 50)
    private String gateway;
    
    @Column(name = "gateway_txn_id", length = 100)
    private String gatewayTxnId;
    
    @Column(name = "cash_denomination", columnDefinition = "TEXT")
    private String cashDenomination;
    
    @Column(name = "received_by", length = 100)
    private String receivedBy;
    
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
    
    @Column(name = "status", length = 20)
    private String status = "COMPLETED";
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
}
