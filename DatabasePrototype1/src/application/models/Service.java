package application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

public class Service {
    private Long id;
    private String name;
    private String type;
    private String description;
    private BigDecimal price;
    private Boolean taxable; // Keep for backward compatibility
    private Boolean active;  // Add for database schema
    private Timestamp createdAt;

    // JavaFX Properties (lazy initialization)
    private StringProperty nameProperty;
    private StringProperty typeProperty;
    private StringProperty descriptionProperty;
    private StringProperty priceProperty;
    private StringProperty taxableProperty;
    private StringProperty activeProperty;

    // Constructors
    public Service() {
        this.active = true;
        this.taxable = false;
        this.price = BigDecimal.ZERO;
    }

    public Service(String name, String description, BigDecimal price, Boolean taxable) {
        this();
        this.name = name;
        this.description = description;
        this.price = price;
        this.taxable = taxable;
    }

    // Getters and Setters (maintain compatibility)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name; 
        if (nameProperty != null) {
            nameProperty.set(name);
        }
    }

    public String getType() { return type; }
    public void setType(String type) { 
        this.type = type; 
        if (typeProperty != null) {
            typeProperty.set(type);
        }
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { 
        this.description = description; 
        if (descriptionProperty != null) {
            descriptionProperty.set(description);
        }
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price; 
        if (priceProperty != null) {
            priceProperty.set(price != null ? "₹" + String.format("%.2f", price.doubleValue()) : "₹0.00");
        }
    }

    public Boolean getTaxable() { return taxable; }
    public void setTaxable(Boolean taxable) { 
        this.taxable = taxable; 
        if (taxableProperty != null) {
            taxableProperty.set(taxable != null && taxable ? "Yes" : "No");
        }
    }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { 
        this.active = active; 
        if (activeProperty != null) {
            activeProperty.set(active != null && active ? "Yes" : "No");
        }
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // JavaFX Property getters (lazy initialization)
    public StringProperty nameProperty() { 
        if (nameProperty == null) {
            nameProperty = new SimpleStringProperty(name);
        }
        return nameProperty; 
    }

    public StringProperty typeProperty() { 
        if (typeProperty == null) {
            typeProperty = new SimpleStringProperty(type);
        }
        return typeProperty; 
    }

    public StringProperty descriptionProperty() { 
        if (descriptionProperty == null) {
            descriptionProperty = new SimpleStringProperty(description);
        }
        return descriptionProperty; 
    }

    public StringProperty priceProperty() { 
        if (priceProperty == null) {
            priceProperty = new SimpleStringProperty(
                price != null ? "₹" + String.format("%.2f", price.doubleValue()) : "₹0.00"
            );
        }
        return priceProperty; 
    }

    public StringProperty taxProperty() { 
        if (taxableProperty == null) {
            taxableProperty = new SimpleStringProperty(taxable != null && taxable ? "Yes" : "No");
        }
        return taxableProperty; 
    }

    public StringProperty availableProperty() { 
        if (activeProperty == null) {
            activeProperty = new SimpleStringProperty(active != null && active ? "Yes" : "No");
        }
        return activeProperty; 
    }

    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", taxable=" + taxable +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(id, service.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
