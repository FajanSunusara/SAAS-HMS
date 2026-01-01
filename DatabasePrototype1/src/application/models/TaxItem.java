package application.models;

import javafx.beans.property.SimpleDoubleProperty;

public class TaxItem {
    private final SimpleDoubleProperty amount;
    private final SimpleDoubleProperty cgst;
    private final SimpleDoubleProperty sgst;
    private final SimpleDoubleProperty total;
    
    public TaxItem(double amount, double cgst, double sgst, double total) {
        this.amount = new SimpleDoubleProperty(amount);
        this.cgst = new SimpleDoubleProperty(cgst);
        this.sgst = new SimpleDoubleProperty(sgst);
        this.total = new SimpleDoubleProperty(total);
    }
    
    // Getters and Setters
    public double getAmount() { return amount.get(); }
    public void setAmount(double amount) { this.amount.set(amount); }
    public SimpleDoubleProperty amountProperty() { return amount; }
    
    public double getCgst() { return cgst.get(); }
    public void setCgst(double cgst) { this.cgst.set(cgst); }
    public SimpleDoubleProperty cgstProperty() { return cgst; }
    
    public double getSgst() { return sgst.get(); }
    public void setSgst(double sgst) { this.sgst.set(sgst); }
    public SimpleDoubleProperty sgstProperty() { return sgst; }
    
    public double getTotal() { return total.get(); }
    public void setTotal(double total) { this.total.set(total); }
    public SimpleDoubleProperty totalProperty() { return total; }
}
