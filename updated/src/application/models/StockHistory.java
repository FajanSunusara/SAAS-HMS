package application.models;

import javafx.beans.property.*;

public class StockHistory {
    private final StringProperty date;
    private final StringProperty category;
    private final StringProperty item;
    private final StringProperty action;
    private final IntegerProperty qty;
    private final StringProperty assignedTo;
    private final StringProperty handledBy;

    public StockHistory(String date, String category, String item, String action, int qty, String assignedTo, String handledBy) {
        this.date = new SimpleStringProperty(date);
        this.category = new SimpleStringProperty(category);
        this.item = new SimpleStringProperty(item);
        this.action = new SimpleStringProperty(action);
        this.qty = new SimpleIntegerProperty(qty);
        this.assignedTo = new SimpleStringProperty(assignedTo);
        this.handledBy = new SimpleStringProperty(handledBy);
    }

    // Getters
    public String getDate() { return date.get(); }
    public String getCategory() { return category.get(); }
    public String getItem() { return item.get(); }
    public String getAction() { return action.get(); }
    public int getQty() { return qty.get(); }
    public String getAssignedTo() { return assignedTo.get(); }
    public String getHandledBy() { return handledBy.get(); }

    // Properties (for TableView binding)
    public StringProperty dateProperty() { return date; }
    public StringProperty categoryProperty() { return category; }
    public StringProperty itemProperty() { return item; }
    public StringProperty actionProperty() { return action; }
    public IntegerProperty qtyProperty() { return qty; }
    public StringProperty assignedToProperty() { return assignedTo; }
    public StringProperty handledByProperty() { return handledBy; }
}