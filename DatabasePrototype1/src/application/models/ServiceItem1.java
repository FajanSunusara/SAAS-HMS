package application.models;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ServiceItem1 {
    private final SimpleIntegerProperty srNo;
    private final SimpleStringProperty description;
    private final SimpleIntegerProperty quantity;
    private final SimpleDoubleProperty rate;
    private final SimpleDoubleProperty amount;
    
    public ServiceItem1(int srNo, String description, int quantity, double rate, double amount) {
        this.srNo = new SimpleIntegerProperty(srNo);
        this.description = new SimpleStringProperty(description);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.rate = new SimpleDoubleProperty(rate);
        this.amount = new SimpleDoubleProperty(amount);
    }
    
    // Getters and Setters
    public int getSrNo() { return srNo.get(); }
    public void setSrNo(int srNo) { this.srNo.set(srNo); }
    public SimpleIntegerProperty srNoProperty() { return srNo; }
    
    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }
    public SimpleStringProperty descriptionProperty() { return description; }
    
    public int getQuantity() { return quantity.get(); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public SimpleIntegerProperty quantityProperty() { return quantity; }
    
    public double getRate() { return rate.get(); }
    public void setRate(double rate) { this.rate.set(rate); }
    public SimpleDoubleProperty rateProperty() { return rate; }
    
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public SimpleDoubleProperty amountProperty() { return amount; }
}
