package application.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class RefundContext {
    private Long bookingId;
    private Long reservationId;
    private Long guestId;
    private Long originalPaymentId;
    private BigDecimal originalPaymentAmount;
    private BigDecimal refundAmount;
    private String refundType;
    private String refundReason;
    private String refundMethod;
    private BigDecimal processingFee;
    private LocalDate refundDate;
    
    // Additional properties storage
    private Map<String, Object> additionalProperties = new HashMap<>();
    
    // Default constructor
    public RefundContext() {
        this.refundDate = LocalDate.now();
        this.processingFee = BigDecimal.ZERO;
    }
    
    // Constructor with basic parameters
    public RefundContext(Long bookingId, Long originalPaymentId, BigDecimal originalPaymentAmount) {
        this();
        this.bookingId = bookingId;
        this.originalPaymentId = originalPaymentId;
        this.originalPaymentAmount = originalPaymentAmount;
    }
    
    // Static factory method for booking refund
    public static RefundContext forBookingRefund(Long bookingId, Long paymentId, BigDecimal originalAmount) {
        RefundContext context = new RefundContext(bookingId, paymentId, originalAmount);
        return context;
    }
    
    // Static factory method for reservation refund
    public static RefundContext forReservationRefund(Long reservationId, Long paymentId, BigDecimal originalAmount) {
        RefundContext context = new RefundContext(null, paymentId, originalAmount);
        context.setReservationId(reservationId);
        return context;
    }
    
    // Static factory methods
    public static RefundContext forBookingRefund(Long bookingId) {
        RefundContext context = new RefundContext();
        context.setBookingId(bookingId);
        return context;
    }
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }
    
    public Long getOriginalPaymentId() { return originalPaymentId; }
    public void setOriginalPaymentId(Long originalPaymentId) { this.originalPaymentId = originalPaymentId; }
    
    public BigDecimal getOriginalPaymentAmount() { return originalPaymentAmount; }
    public void setOriginalPaymentAmount(BigDecimal originalPaymentAmount) { 
        this.originalPaymentAmount = originalPaymentAmount; 
    }
    
    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
    
    public String getRefundType() { return refundType; }
    public void setRefundType(String refundType) { this.refundType = refundType; }
    
    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }
    
    public String getRefundMethod() { return refundMethod; }
    public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
    
    public BigDecimal getProcessingFee() { return processingFee; }
    public void setProcessingFee(BigDecimal processingFee) { this.processingFee = processingFee; }
    
    public LocalDate getRefundDate() { return refundDate; }
    public void setRefundDate(LocalDate refundDate) { this.refundDate = refundDate; }
    
    // Additional properties methods
    public void setAdditionalProperty(String key, Object value) {
        additionalProperties.put(key, value);
    }
    
    public Object getAdditionalProperty(String key) {
        return additionalProperties.get(key);
    }
    
    public <T> T getAdditionalProperty(String key, Class<T> type) {
        Object value = additionalProperties.get(key);
        return type.isInstance(value) ? type.cast(value) : null;
    }
    
    public boolean hasAdditionalProperty(String key) {
        return additionalProperties.containsKey(key);
    }
    
    public Map<String, Object> getAllAdditionalProperties() {
        return new HashMap<>(additionalProperties);
    }
}
