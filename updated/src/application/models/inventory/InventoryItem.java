package application.models.inventory;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventoryItem {
    private final IntegerProperty itemId = new SimpleIntegerProperty();
    private final StringProperty itemName = new SimpleStringProperty();
    private final StringProperty category = new SimpleStringProperty();
    private final StringProperty location = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty availableQuantity = new SimpleIntegerProperty();
    private final IntegerProperty reservedQuantity = new SimpleIntegerProperty();
    private final IntegerProperty minStockLevel = new SimpleIntegerProperty();
    private final IntegerProperty maxStockLevel = new SimpleIntegerProperty();
    private final ObjectProperty<BigDecimal> unitPrice = new SimpleObjectProperty<>();
    private final StringProperty unit = new SimpleStringProperty();
    private final StringProperty supplier = new SimpleStringProperty();
    private final StringProperty supplierContact = new SimpleStringProperty();
    private final StringProperty sku = new SimpleStringProperty();
    private final StringProperty barcode = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> lastUpdated = new SimpleObjectProperty<>();
    private final StringProperty updatedBy = new SimpleStringProperty();
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();

    // Constructors
    public InventoryItem() {
        // Set default values
        setAvailableQuantity(0);
        setReservedQuantity(0);
        setMinStockLevel(10);
        setMaxStockLevel(1000);
        setUnitPrice(BigDecimal.ZERO);
        setUnit("pieces");
        setActive(true);
        setCreatedAt(LocalDateTime.now());
        setLastUpdated(LocalDateTime.now());
    }

    public InventoryItem(String itemName, String category, String location, 
                        int availableQuantity, int minStockLevel, BigDecimal unitPrice) {
        this();
        setItemName(itemName);
        setCategory(category);
        setLocation(location);
        setAvailableQuantity(availableQuantity);
        setMinStockLevel(minStockLevel);
        setUnitPrice(unitPrice);
    }

    // Property getters
    public IntegerProperty itemIdProperty() { return itemId; }
    public StringProperty itemNameProperty() { return itemName; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty locationProperty() { return location; }
    public StringProperty descriptionProperty() { return description; }
    public IntegerProperty availableQuantityProperty() { return availableQuantity; }
    public IntegerProperty reservedQuantityProperty() { return reservedQuantity; }
    public IntegerProperty minStockLevelProperty() { return minStockLevel; }
    public IntegerProperty maxStockLevelProperty() { return maxStockLevel; }
    public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }
    public StringProperty unitProperty() { return unit; }
    public StringProperty supplierProperty() { return supplier; }
    public StringProperty supplierContactProperty() { return supplierContact; }
    public StringProperty skuProperty() { return sku; }
    public StringProperty barcodeProperty() { return barcode; }
    public ObjectProperty<LocalDateTime> lastUpdatedProperty() { return lastUpdated; }
    public StringProperty updatedByProperty() { return updatedBy; }
    public BooleanProperty activeProperty() { return active; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }

    // Getters and Setters
    public int getItemId() { return itemId.get(); }
    public void setItemId(int itemId) { this.itemId.set(itemId); }

    public String getItemName() { return itemName.get(); }
    public void setItemName(String itemName) { this.itemName.set(itemName != null ? itemName : ""); }

    public String getCategory() { return category.get(); }
    public void setCategory(String category) { this.category.set(category != null ? category : ""); }

    public String getLocation() { return location.get(); }
    public void setLocation(String location) { this.location.set(location != null ? location : ""); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description != null ? description : ""); }

    public int getAvailableQuantity() { return availableQuantity.get(); }
    public void setAvailableQuantity(int availableQuantity) { this.availableQuantity.set(Math.max(0, availableQuantity)); }

    public int getReservedQuantity() { return reservedQuantity.get(); }
    public void setReservedQuantity(int reservedQuantity) { this.reservedQuantity.set(Math.max(0, reservedQuantity)); }

    public int getMinStockLevel() { return minStockLevel.get(); }
    public void setMinStockLevel(int minStockLevel) { this.minStockLevel.set(Math.max(0, minStockLevel)); }

    public int getMaxStockLevel() { return maxStockLevel.get(); }
    public void setMaxStockLevel(int maxStockLevel) { this.maxStockLevel.set(Math.max(1, maxStockLevel)); }

    public BigDecimal getUnitPrice() { return unitPrice.get(); }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice.set(unitPrice != null ? unitPrice : BigDecimal.ZERO); 
    }

    public String getUnit() { return unit.get(); }
    public void setUnit(String unit) { this.unit.set(unit != null ? unit : "pieces"); }

    public String getSupplier() { return supplier.get(); }
    public void setSupplier(String supplier) { this.supplier.set(supplier != null ? supplier : ""); }

    public String getSupplierContact() { return supplierContact.get(); }
    public void setSupplierContact(String supplierContact) { 
        this.supplierContact.set(supplierContact != null ? supplierContact : ""); 
    }

    public String getSku() { return sku.get(); }
    public void setSku(String sku) { this.sku.set(sku != null ? sku : ""); }

    public String getBarcode() { return barcode.get(); }
    public void setBarcode(String barcode) { this.barcode.set(barcode != null ? barcode : ""); }

    public LocalDateTime getLastUpdated() { return lastUpdated.get(); }
    public void setLastUpdated(LocalDateTime lastUpdated) { 
        this.lastUpdated.set(lastUpdated != null ? lastUpdated : LocalDateTime.now()); 
    }

    public String getUpdatedBy() { return updatedBy.get(); }
    public void setUpdatedBy(String updatedBy) { this.updatedBy.set(updatedBy != null ? updatedBy : ""); }

    public boolean isActive() { return active.get(); }
    public void setActive(boolean active) { this.active.set(active); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt.set(createdAt != null ? createdAt : LocalDateTime.now()); 
    }

    // Utility methods
    public BigDecimal getTotalValue() {
        BigDecimal price = getUnitPrice();
        if (price == null) price = BigDecimal.ZERO;
        return price.multiply(BigDecimal.valueOf(getAvailableQuantity()));
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= getMinStockLevel() && getAvailableQuantity() > 0;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return "Out of Stock";
        } else if (isLowStock()) {
            return "Low Stock";
        } else {
            return "Available";
        }
    }

    public int getTotalQuantity() {
        return getAvailableQuantity() + getReservedQuantity();
    }

    // Validation methods
    public boolean isValid() {
        return getItemName() != null && !getItemName().trim().isEmpty() &&
               getCategory() != null && !getCategory().trim().isEmpty() &&
               getUnitPrice() != null && getUnitPrice().compareTo(BigDecimal.ZERO) >= 0 &&
               getMinStockLevel() >= 0 &&
               getMaxStockLevel() > getMinStockLevel();
    }

    // Clone method
    public InventoryItem clone() {
        InventoryItem cloned = new InventoryItem();
        cloned.setItemId(this.getItemId());
        cloned.setItemName(this.getItemName());
        cloned.setCategory(this.getCategory());
        cloned.setLocation(this.getLocation());
        cloned.setDescription(this.getDescription());
        cloned.setAvailableQuantity(this.getAvailableQuantity());
        cloned.setReservedQuantity(this.getReservedQuantity());
        cloned.setMinStockLevel(this.getMinStockLevel());
        cloned.setMaxStockLevel(this.getMaxStockLevel());
        cloned.setUnitPrice(this.getUnitPrice());
        cloned.setUnit(this.getUnit());
        cloned.setSupplier(this.getSupplier());
        cloned.setSupplierContact(this.getSupplierContact());
        cloned.setSku(this.getSku());
        cloned.setBarcode(this.getBarcode());
        cloned.setLastUpdated(this.getLastUpdated());
        cloned.setUpdatedBy(this.getUpdatedBy());
        cloned.setActive(this.isActive());
        cloned.setCreatedAt(this.getCreatedAt());
        return cloned;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", 
            getItemName() != null ? getItemName() : "Unknown Item", 
            getCategory() != null ? getCategory() : "No Category",
            getSku() != null && !getSku().isEmpty() ? getSku() : "No SKU");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        InventoryItem that = (InventoryItem) obj;
        return getItemId() == that.getItemId();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(getItemId());
    }
}
