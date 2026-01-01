package application.models;

import java.math.BigDecimal;
import java.util.Objects;

public class ServiceItem {
    private Long id;
    private Long serviceUsedId;
    private String itemName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal amount;

    // Constructors
    public ServiceItem() {
        this.quantity = 0;
        this.price = BigDecimal.ZERO;
        this.amount = BigDecimal.ZERO;
    }

    public ServiceItem(String itemName, Integer quantity, BigDecimal price) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.amount = calculateAmount();
    }

    public ServiceItem(Long serviceUsedId, String itemName, Integer quantity, BigDecimal price) {
        this(itemName, quantity, price);
        this.serviceUsedId = serviceUsedId;
    }

    // Helper method to calculate amount
    private BigDecimal calculateAmount() {
        if (quantity != null && price != null) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    // Helper method to recalculate amount when quantity or price changes
    public void recalculateAmount() {
        this.amount = calculateAmount();
    }

    // Validation methods
    public boolean isValid() {
        return itemName != null && !itemName.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               price != null && price.compareTo(BigDecimal.ZERO) >= 0;
    }

    public String getFormattedAmount() {
        return "₹" + String.format("%.2f", amount.doubleValue());
    }

    public String getFormattedPrice() {
        return "₹" + String.format("%.2f", price.doubleValue());
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getServiceUsedId() { return serviceUsedId; }
    public void setServiceUsedId(Long serviceUsedId) { this.serviceUsedId = serviceUsedId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { 
        this.quantity = quantity; 
        recalculateAmount();
    }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { 
        this.price = price; 
        recalculateAmount();
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "ServiceItem{" +
                "id=" + id +
                ", serviceUsedId=" + serviceUsedId +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", amount=" + amount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceItem that = (ServiceItem) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
