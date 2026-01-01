package application.models;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PaymentHistoryEntry {
    private final StringProperty date;
    private final DoubleProperty amount;
    private final StringProperty description;

    public PaymentHistoryEntry(String date, double amount, String description) {
        this.date = new SimpleStringProperty(date);
        this.amount = new SimpleDoubleProperty(amount);
        this.description = new SimpleStringProperty(description);
    }

    public StringProperty dateProperty() { return date; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty descriptionProperty() { return description; }

    public String getDate() { return date.get(); }
    public double getAmount() { return amount.get(); }
    public String getDescription() { return description.get(); }
}
