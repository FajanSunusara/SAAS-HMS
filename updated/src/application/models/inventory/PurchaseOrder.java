package application.models.inventory;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PurchaseOrder {
    private final IntegerProperty poId = new SimpleIntegerProperty();
    private final StringProperty poNumber = new SimpleStringProperty();
    private final IntegerProperty vendorId = new SimpleIntegerProperty();
    private final StringProperty vendorName = new SimpleStringProperty();
    private final StringProperty vendorContact = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> orderDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> expectedDate = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDate> deliveryDate = new SimpleObjectProperty<>();
    private final IntegerProperty totalItems = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> totalAmount = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> taxAmount = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> finalAmount = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty paymentTerms = new SimpleStringProperty();
    private final StringProperty shippingAddress = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final StringProperty createdBy = new SimpleStringProperty();
    private final StringProperty approvedBy = new SimpleStringProperty();
    private final StringProperty receivedBy = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Constructors
    public PurchaseOrder() {
        setOrderDate(LocalDate.now());
        setStatus("Draft");
        setTotalItems(0);
        setTotalAmount(BigDecimal.ZERO);
        setTaxAmount(BigDecimal.ZERO);
        setFinalAmount(BigDecimal.ZERO);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    public PurchaseOrder(String poNumber, int vendorId, String vendorName) {
        this();
        setPoNumber(poNumber);
        setVendorId(vendorId);
        setVendorName(vendorName);
    }

    // Property getters
    public IntegerProperty poIdProperty() { return poId; }
    public StringProperty poNumberProperty() { return poNumber; }
    public IntegerProperty vendorIdProperty() { return vendorId; }
    public StringProperty vendorNameProperty() { return vendorName; }
    public StringProperty vendorContactProperty() { return vendorContact; }
    public ObjectProperty<LocalDate> orderDateProperty() { return orderDate; }
    public ObjectProperty<LocalDate> expectedDateProperty() { return expectedDate; }
    public ObjectProperty<LocalDate> deliveryDateProperty() { return deliveryDate; }
    public IntegerProperty totalItemsProperty() { return totalItems; }
    public ObjectProperty<BigDecimal> totalAmountProperty() { return totalAmount; }
    public ObjectProperty<BigDecimal> taxAmountProperty() { return taxAmount; }
    public ObjectProperty<BigDecimal> finalAmountProperty() { return finalAmount; }
    public StringProperty statusProperty() { return status; }
    public StringProperty paymentTermsProperty() { return paymentTerms; }
    public StringProperty shippingAddressProperty() { return shippingAddress; }
    public StringProperty notesProperty() { return notes; }
    public StringProperty createdByProperty() { return createdBy; }
    public StringProperty approvedByProperty() { return approvedBy; }
    public StringProperty receivedByProperty() { return receivedBy; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Getters and Setters
    public int getPoId() { return poId.get(); }
    public void setPoId(int poId) { this.poId.set(poId); }

    public String getPoNumber() { return poNumber.get(); }
    public void setPoNumber(String poNumber) { this.poNumber.set(poNumber); }

    public int getVendorId() { return vendorId.get(); }
    public void setVendorId(int vendorId) { this.vendorId.set(vendorId); }

    public String getVendorName() { return vendorName.get(); }
    public void setVendorName(String vendorName) { this.vendorName.set(vendorName); }

    public String getVendorContact() { return vendorContact.get(); }
    public void setVendorContact(String vendorContact) { this.vendorContact.set(vendorContact); }

    public LocalDate getOrderDate() { return orderDate.get(); }
    public void setOrderDate(LocalDate orderDate) { this.orderDate.set(orderDate); }

    public LocalDate getExpectedDate() { return expectedDate.get(); }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate.set(expectedDate); }

    public LocalDate getDeliveryDate() { return deliveryDate.get(); }
    public void setDeliveryDate(LocalDate deliveryDate) { this.deliveryDate.set(deliveryDate); }

    public int getTotalItems() { return totalItems.get(); }
    public void setTotalItems(int totalItems) { this.totalItems.set(totalItems); }

    public BigDecimal getTotalAmount() { return totalAmount.get(); }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount.set(totalAmount); }

    public BigDecimal getTaxAmount() { return taxAmount.get(); }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount.set(taxAmount); }

    public BigDecimal getFinalAmount() { return finalAmount.get(); }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount.set(finalAmount); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getPaymentTerms() { return paymentTerms.get(); }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms.set(paymentTerms); }

    public String getShippingAddress() { return shippingAddress.get(); }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress.set(shippingAddress); }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }

    public String getCreatedBy() { return createdBy.get(); }
    public void setCreatedBy(String createdBy) { this.createdBy.set(createdBy); }

    public String getApprovedBy() { return approvedBy.get(); }
    public void setApprovedBy(String approvedBy) { this.approvedBy.set(approvedBy); }

    public String getReceivedBy() { return receivedBy.get(); }
    public void setReceivedBy(String receivedBy) { this.receivedBy.set(receivedBy); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    // Utility methods
    public boolean isDraft() { return "Draft".equals(status.get()); }
    public boolean isOrdered() { return "Ordered".equals(status.get()); }
    public boolean isReceived() { return "Received".equals(status.get()); }
    public boolean isCancelled() { return "Cancelled".equals(status.get()); }

    @Override
    public String toString() {
        return poNumber.get() + " - " + vendorName.get() + " (" + status.get() + ")";
    }
}
