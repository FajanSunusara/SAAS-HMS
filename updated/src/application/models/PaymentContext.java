package application.models;

import application.enums.PaymentType;
import application.enums.InvoiceType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class PaymentContext {
    private PaymentType paymentType;
    private InvoiceType invoiceType;
    private Long bookingId;
    private Long reservationId;
    private Long guestId;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String paymentMethod;
    private String transactionId;
    
    // Additional properties storage
    private Map<String, Object> additionalProperties = new HashMap<>();
    
    public PaymentContext() {}
    
    public PaymentContext(PaymentType paymentType, InvoiceType invoiceType) {
        this.paymentType = paymentType;
        this.invoiceType = invoiceType;
    }
    
    // Builder pattern methods
    public static PaymentContext forNewBooking() {
        return new PaymentContext(PaymentType.NEW_BOOKING, InvoiceType.BOOKING);
    }
    
    public static PaymentContext forExistingBooking() {
        return new PaymentContext(PaymentType.EXISTING_BOOKING, InvoiceType.BOOKING);
    }
    
    public static PaymentContext forReservation() {
        return new PaymentContext(PaymentType.RESERVATION, InvoiceType.RESERVATION);
    }
    
    public static PaymentContext forRefund() {
        return new PaymentContext(PaymentType.REFUND, InvoiceType.REFUND);
    }
    
    public static PaymentContext forService() {
        return new PaymentContext(PaymentType.SERVICE, InvoiceType.SERVICE);
    }
    
    public static PaymentContext forExpenditure() {
        return new PaymentContext(PaymentType.EXPENDITURE, InvoiceType.EXPENDITURE);
    }
    
    // Getters and setters
    public PaymentType getPaymentType() { return paymentType; }
    public void setPaymentType(PaymentType paymentType) { this.paymentType = paymentType; }
    
    public InvoiceType getInvoiceType() { return invoiceType; }
    public void setInvoiceType(InvoiceType invoiceType) { this.invoiceType = invoiceType; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
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
    
    // Convenience methods for common properties
    public String getGuestName() {
        return getAdditionalProperty("guestName", String.class);
    }
    
    public void setGuestName(String guestName) {
        setAdditionalProperty("guestName", guestName);
    }
    
    public String getRoomNo() {
        return getAdditionalProperty("roomNo", String.class);
    }
    
    public void setRoomNo(String roomNo) {
        setAdditionalProperty("roomNo", roomNo);
    }
}