package application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for payment history records in an invoice
 */
public class PaymentRecord {
    private final StringProperty date;
    private final StringProperty method;
    private final StringProperty reference;
    private final StringProperty amount;

    public PaymentRecord() {
        this("", "", "", "");
    }

    public PaymentRecord(String date, String method, String reference, String amount) {
        this.date = new SimpleStringProperty(date);
        this.method = new SimpleStringProperty(method);
        this.reference = new SimpleStringProperty(reference);
        this.amount = new SimpleStringProperty(amount);
    }

    // Date Property
    public StringProperty dateProperty() {
        return date;
    }

    public String getDate() {
        return date.get();
    }

    public void setDate(String date) {
        this.date.set(date);
    }

    // Method Property
    public StringProperty methodProperty() {
        return method;
    }

    public String getMethod() {
        return method.get();
    }

    public void setMethod(String method) {
        this.method.set(method);
    }

    // Reference Property
    public StringProperty referenceProperty() {
        return reference;
    }

    public String getReference() {
        return reference.get();
    }

    public void setReference(String reference) {
        this.reference.set(reference);
    }

    // Amount Property
    public StringProperty amountProperty() {
        return amount;
    }

    public String getAmount() {
        return amount.get();
    }

    public void setAmount(String amount) {
        this.amount.set(amount);
    }

    @Override
    public String toString() {
        return String.format("PaymentRecord{date='%s', method='%s', reference='%s', amount='%s'}", 
            getDate(), getMethod(), getReference(), getAmount());
    }
}