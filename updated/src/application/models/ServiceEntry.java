package application.models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.List;

/**
 * Model class representing a service entry in the service bill table.
 * Contains a service with multiple line items.
 */
public class ServiceEntry {
    private String date;
    private String name;
    private String type;
    private final ObservableList<ServiceLineItem> lineItems;

    public ServiceEntry() {
        this.lineItems = FXCollections.observableArrayList();
    }

    public ServiceEntry(String date, String name, String type) {
        this();
        this.date = date;
        this.name = name;
        this.type = type;
    }

    public ServiceEntry(String date, String name, String type, List<ServiceLineItem> items) {
        this(date, name, type);
        if (items != null) {
            this.lineItems.addAll(items);
        }
    }

    /**
     * Add a line item to this service entry
     */
    public void addLineItem(ServiceLineItem item) {
        if (item != null) {
            lineItems.add(item);
        }
    }

    /**
     * Remove a line item from this service entry
     */
    public void removeLineItem(ServiceLineItem item) {
        lineItems.remove(item);
    }

    /**
     * Clear all line items
     */
    public void clearLineItems() {
        lineItems.clear();
    }

    /**
     * Calculate total amount from all line items
     */
    public double getAmount() {
        return lineItems.stream()
            .mapToDouble(ServiceLineItem::getTotalAmount)
            .sum();
    }

    /**
     * Calculate total quantity from all line items
     */
    public int getQuantity() {
        return lineItems.stream()
            .mapToInt(ServiceLineItem::getQuantity)
            .sum();
    }

    /**
     * Get the number of line items
     */
    public int getLineItemCount() {
        return lineItems.size();
    }

    /**
     * Check if this service entry has any line items
     */
    public boolean hasLineItems() {
        return !lineItems.isEmpty();
    }

    /**
     * Get a formatted description of all line items
     */
    public String getFormattedDetails() {
        if (lineItems.isEmpty()) {
            return "No items";
        }
        
        StringBuilder details = new StringBuilder();
        for (int i = 0; i < lineItems.size(); i++) {
            ServiceLineItem item = lineItems.get(i);
            details.append(item.getItemName())
                   .append(" x")
                   .append(item.getQuantity());
            if (i < lineItems.size() - 1) {
                details.append(", ");
            }
        }
        return details.toString();
    }

    // Getters and Setters
    public String getDate() { 
        return date; 
    }
    
    public void setDate(String date) { 
        this.date = date; 
    }

    public String getName() { 
        return name; 
    }
    
    public void setName(String name) { 
        this.name = name; 
    }

    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }

    public ObservableList<ServiceLineItem> getLineItems() { 
        return lineItems; 
    }

    @Override
    public String toString() {
        return "ServiceEntry{" +
                "date='" + date + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + String.format("%.2f", getAmount()) +
                ", quantity=" + getQuantity() +
                ", lineItems=" + lineItems.size() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        ServiceEntry that = (ServiceEntry) obj;
        return java.util.Objects.equals(date, that.date) &&
               java.util.Objects.equals(name, that.name) &&
               java.util.Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, name, type);
    }
}
