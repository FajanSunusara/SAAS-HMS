package application.models;

import javafx.beans.property.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.Objects;

public class Invoice {
    // JavaFX Properties for binding
    private final LongProperty id = new SimpleLongProperty();
    private final LongProperty bookingId = new SimpleLongProperty();
    private final LongProperty guestId = new SimpleLongProperty();
    private final StringProperty invoiceNumber = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> invoiceDate = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> subtotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> gst = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> paidAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> dueAmount = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    
    // Display fields for UI
    private final StringProperty guestName = new SimpleStringProperty("");
    private final StringProperty guestEmail = new SimpleStringProperty("");
    private final StringProperty guestPhone = new SimpleStringProperty("");
    private final StringProperty roomNo = new SimpleStringProperty("");
    private final ObjectProperty<LocalDate> checkInDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> checkOutDate = new SimpleObjectProperty<>();
    private final StringProperty paymentStatus = new SimpleStringProperty("");
    private final StringProperty paymentMethod = new SimpleStringProperty("");
    

    // Legacy fields for database compatibility
    private Timestamp issueDate;
    private Timestamp dueDate;
    private Timestamp createdAt;

    // Constructors
    public Invoice() {}

    public Invoice(Long bookingId, BigDecimal totalAmount, BigDecimal paidAmount, 
                   BigDecimal remainingAmount, String status, Timestamp issueDate, Timestamp dueDate) {
        setBookingId(bookingId);
        setTotal(totalAmount);
        setPaidAmount(paidAmount);
        setDueAmount(remainingAmount);
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    // Constructor for sample data compatibility
    public Invoice(String invoiceNumber, String customerName, LocalDate date, 
                   double amount, double taxPercentage, String status, String paymentType) {
        setInvoiceNumber(invoiceNumber);
        setGuestName(customerName);
        setInvoiceDate(date);
        setPaymentMethod(paymentType);
        
        // Calculate values
        BigDecimal subtotalValue = BigDecimal.valueOf(amount);
        BigDecimal gstValue = subtotalValue.multiply(BigDecimal.valueOf(taxPercentage / 100.0));
        BigDecimal totalValue = subtotalValue.add(gstValue);
        
        setSubtotal(subtotalValue);
        setGst(gstValue);
        setTotal(totalValue);
        
        // Set payment status
        if ("Paid".equals(status)) {
            setPaidAmount(totalValue);
            setDueAmount(BigDecimal.ZERO);
        } else {
            setPaidAmount(BigDecimal.ZERO);
            setDueAmount(totalValue);
        }
    }

    // Property methods for JavaFX binding
    public LongProperty idProperty() { return id; }
    public LongProperty bookingIdProperty() { return bookingId; }
    public LongProperty guestIdProperty() { return guestId; }
    public StringProperty invoiceNumberProperty() { return invoiceNumber; }
    public ObjectProperty<LocalDate> invoiceDateProperty() { return invoiceDate; }
    public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }
    public ObjectProperty<BigDecimal> gstProperty() { return gst; }
    public ObjectProperty<BigDecimal> totalProperty() { return total; }
    public ObjectProperty<BigDecimal> paidAmountProperty() { return paidAmount; }
    public ObjectProperty<BigDecimal> dueAmountProperty() { return dueAmount; }
    public BooleanProperty selectedProperty() { return selected; }
    
    public StringProperty guestNameProperty() { return guestName; }
    public StringProperty guestEmailProperty() { return guestEmail; }
    public StringProperty guestPhoneProperty() { return guestPhone; }
    public StringProperty roomNoProperty() { return roomNo; }
    public ObjectProperty<LocalDate> checkInDateProperty() { return checkInDate; }
    public ObjectProperty<LocalDate> checkOutDateProperty() { return checkOutDate; }
    public StringProperty paymentStatusProperty() { return paymentStatus; }
    public StringProperty paymentMethodProperty() { return paymentMethod; }

    // Standard getters and setters
    public Long getId() { return id.get(); }
    public void setId(Long id) { this.id.set(id); }

    public Long getBookingId() { return bookingId.get(); }
    public void setBookingId(Long bookingId) { this.bookingId.set(bookingId); }

    public Long getGuestId() { return guestId.get(); }
    public void setGuestId(Long guestId) { this.guestId.set(guestId); }

    public String getInvoiceNumber() { return invoiceNumber.get(); }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber.set(invoiceNumber != null ? invoiceNumber : ""); }

    public LocalDate getInvoiceDate() { return invoiceDate.get(); }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate.set(invoiceDate); }

    public BigDecimal getSubtotal() { return subtotal.get(); }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal.set(subtotal != null ? subtotal : BigDecimal.ZERO); }

    public BigDecimal getGst() { return gst.get(); }
    public void setGst(BigDecimal gst) { this.gst.set(gst != null ? gst : BigDecimal.ZERO); }

    public BigDecimal getTotal() { return total.get(); }
    public void setTotal(BigDecimal total) { this.total.set(total != null ? total : BigDecimal.ZERO); }

    public BigDecimal getPaidAmount() { return paidAmount.get(); }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount.set(paidAmount != null ? paidAmount : BigDecimal.ZERO); }

    public BigDecimal getDueAmount() { return dueAmount.get(); }
    public void setDueAmount(BigDecimal dueAmount) { this.dueAmount.set(dueAmount != null ? dueAmount : BigDecimal.ZERO); }

    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean selected) { this.selected.set(selected); }

    public String getGuestName() { return guestName.get(); }
    public void setGuestName(String guestName) { this.guestName.set(guestName != null ? guestName : ""); }

    public String getGuestEmail() { return guestEmail.get(); }
    public void setGuestEmail(String guestEmail) { this.guestEmail.set(guestEmail != null ? guestEmail : ""); }

    public String getGuestPhone() { return guestPhone.get(); }
    public void setGuestPhone(String guestPhone) { this.guestPhone.set(guestPhone != null ? guestPhone : ""); }

    public String getRoomNo() { return roomNo.get(); }
    public void setRoomNo(String roomNo) { this.roomNo.set(roomNo != null ? roomNo : ""); }

    public LocalDate getCheckInDate() { return checkInDate.get(); }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate.set(checkInDate); }

    public LocalDate getCheckOutDate() { return checkOutDate.get(); }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate.set(checkOutDate); }

    public String getPaymentStatus() { return paymentStatus.get(); }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus.set(paymentStatus != null ? paymentStatus : ""); }

    public String getPaymentMethod() { return paymentMethod.get(); }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod.set(paymentMethod != null ? paymentMethod : ""); }

    // Legacy getters and setters for database compatibility
    public Timestamp getIssueDate() { return issueDate; }
    public void setIssueDate(Timestamp issueDate) { this.issueDate = issueDate; }

    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // Compatibility methods for existing controller
    public String getCustomerName() { return getGuestName(); }
    public void setCustomerName(String name) { setGuestName(name); }
    
    public LocalDate getDate() { return getInvoiceDate(); }
    public void setDate(LocalDate date) { setInvoiceDate(date); }
    
    public double getAmount() { 
        BigDecimal sub = getSubtotal();
        return sub != null ? sub.doubleValue() : 0.0; 
    }

    public String getInvoiceId() { return getInvoiceNumber(); }
    public void setInvoiceId(String invoiceId) { setInvoiceNumber(invoiceId); }

    public String getPaymentType() { return getPaymentMethod(); }
    public void setPaymentType(String paymentType) { setPaymentMethod(paymentType); }

    // Computed properties
    public double getTaxPercentage() {
        BigDecimal sub = getSubtotal();
        BigDecimal gstValue = getGst();
        
        if (sub != null && gstValue != null && sub.doubleValue() > 0) {
            return (gstValue.doubleValue() / sub.doubleValue()) * 100;
        }
        return 18.0; // Default GST rate
    }

    public String getStatus() {
        BigDecimal totalValue = getTotal();
        BigDecimal paid = getPaidAmount();
        
        if (totalValue == null || totalValue.compareTo(BigDecimal.ZERO) <= 0) {
            return "N/A";
        }
        
        if (paid != null && paid.compareTo(totalValue) >= 0) {
            return "Paid";
        } else if (paid == null || paid.compareTo(BigDecimal.ZERO) <= 0) {
            return "Unpaid";
        } else {
            return "Partial";
        }
    }

    // Legacy method for compatibility
    public BigDecimal getTotalAmount() { return getTotal(); }
    public void setTotalAmount(BigDecimal totalAmount) { setTotal(totalAmount); }

    public BigDecimal getRemainingAmount() { return getDueAmount(); }
    public void setRemainingAmount(BigDecimal remainingAmount) { setDueAmount(remainingAmount); }

    @Override
    public String toString() {
        return "Invoice{" +
            "id=" + getId() +
            ", invoiceNumber='" + getInvoiceNumber() + '\'' +
            ", guestName='" + getGuestName() + '\'' +
            ", total=" + getTotal() +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Invoice invoice = (Invoice) o;
        return Objects.equals(getId(), invoice.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    
}
