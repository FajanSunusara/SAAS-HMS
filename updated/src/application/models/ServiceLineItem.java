package application.models;

import javafx.beans.property.*;

public class ServiceLineItem {
    private final StringProperty itemName;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final DoubleProperty totalAmount;

    public ServiceLineItem() {
        this.itemName = new SimpleStringProperty();
        this.quantity = new SimpleIntegerProperty();
        this.unitPrice = new SimpleDoubleProperty();
        this.totalAmount = new SimpleDoubleProperty();
        
        // Bind total amount to quantity * unit price
        this.totalAmount.bind(quantity.multiply(unitPrice));
    }

    public ServiceLineItem(String itemName, int quantity, double unitPrice) {
        this();
        setItemName(itemName);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    // Property getters for JavaFX binding
    public StringProperty itemNameProperty() { return itemName; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public DoubleProperty totalAmountProperty() { return totalAmount; }

    // Value getters
    public String getItemName() { return itemName.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public double getTotalAmount() { return totalAmount.get(); }

    // Setters
    public void setItemName(String itemName) { 
        this.itemName.set(itemName == null ? "" : itemName); 
    }
    public void setQuantity(int quantity) { 
        this.quantity.set(Math.max(0, quantity)); 
    }
    public void setUnitPrice(double unitPrice) { 
        this.unitPrice.set(Math.max(0.0, unitPrice)); 
    }

    @Override
    public String toString() {
        return "ServiceLineItem{" +
                "itemName='" + getItemName() + '\'' +
                ", quantity=" + getQuantity() +
                ", unitPrice=" + String.format("%.2f", getUnitPrice()) +
                ", totalAmount=" + String.format("%.2f", getTotalAmount()) +
                '}';
    }
}
