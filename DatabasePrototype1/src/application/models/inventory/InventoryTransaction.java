package application.models.inventory;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryTransaction {
    private final IntegerProperty transactionId = new SimpleIntegerProperty();
    private final StringProperty transNumber = new SimpleStringProperty();
    private final IntegerProperty itemId = new SimpleIntegerProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty transactionType = new SimpleStringProperty();
    private final IntegerProperty quantity = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();
    private final ObjectProperty<BigDecimal> totalValue = new SimpleObjectProperty<>();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty referenceNumber = new SimpleStringProperty();
    private final StringProperty referenceType = new SimpleStringProperty();
    private final IntegerProperty referenceId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDateTime> transactionDate = new SimpleObjectProperty<>();
    private final StringProperty handledBy = new SimpleStringProperty();
    private final StringProperty notes = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    // Constructors
    public InventoryTransaction() {
        setTransactionDate(LocalDateTime.now());
        setStatus("Completed");
    }

    public InventoryTransaction(int itemId, String transactionType, int quantity) {
        this();
        setItemId(itemId);
        setTransactionType(transactionType);
        setQuantity(quantity);
    }

    // Property getters
    public IntegerProperty transactionIdProperty() { return transactionId; }
    public StringProperty transNumberProperty() { return transNumber; }
    public IntegerProperty itemIdProperty() { return itemId; }
    public StringProperty itemNameProperty() { return itemName; }
    public StringProperty transactionTypeProperty() { return transactionType; }
    public IntegerProperty quantityProperty() { return quantity; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
    public ObjectProperty<BigDecimal> totalValueProperty() { return totalValue; }
    public StringProperty locationProperty() { return location; }
    public StringProperty referenceNumberProperty() { return referenceNumber; }
    public StringProperty referenceTypeProperty() { return referenceType; }
    public IntegerProperty referenceIdProperty() { return referenceId; }
    public ObjectProperty<LocalDateTime> transactionDateProperty() { return transactionDate; }
    public StringProperty handledByProperty() { return handledBy; }
    public StringProperty notesProperty() { return notes; }
    public StringProperty statusProperty() { return status; }

    // Getters and Setters
    public int getTransactionId() { return transactionId.get(); }
    public void setTransactionId(int transactionId) { this.transactionId.set(transactionId); }

    public String getTransNumber() { return transNumber.get(); }
    public void setTransNumber(String transNumber) { this.transNumber.set(transNumber); }

    public int getItemId() { return itemId.get(); }
    public void setItemId(int itemId) { this.itemId.set(itemId); }

    public String getItemName() { return itemName.get(); }
    public void setItemName(String itemName) { this.itemName.set(itemName); }

    public String getTransactionType() { return transactionType.get(); }
    public void setTransactionType(String transactionType) { this.transactionType.set(transactionType); }

    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }

    public BigDecimal getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice.set(unitPrice); }

    public BigDecimal getTotalValue() { return totalValue.get(); }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue.set(totalValue); }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location); }

    public String getReferenceNumber() { return referenceNumber.get(); }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber.set(referenceNumber); }

    public String getReferenceType() { return referenceType.get(); }
    public void setReferenceType(String referenceType) { this.referenceType.set(referenceType); }

    public int getReferenceId() { return referenceId.get(); }
    public void setReferenceId(int referenceId) { this.referenceId.set(referenceId); }

    public LocalDateTime getTransactionDate() { return transactionDate.get(); }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate.set(transactionDate); }

    public String getHandledBy() { return handledBy.get(); }
    public void setHandledBy(String handledBy) { this.handledBy.set(handledBy); }

    public String getNotes() { return notes.get(); }
    public void setNotes(String notes) { this.notes.set(notes); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    @Override
    public String toString() {
        return transNumber.get() + " - " + transactionType.get() + " (" + quantity.get() + ")";
    }
}
