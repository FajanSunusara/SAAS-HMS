package application.models;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ServiceUsed {
    private Long id;
    private Long bookingId;
    private Date serviceDate;
    private String service;
    private String category;
    private Integer totalQty;
    private BigDecimal totalAmount;
    private Timestamp createdAt;
    
    // For UI - list of service items
    private List<ServiceItem> serviceItems = new ArrayList<>();

    // Constructors
    public ServiceUsed() {
        this.totalQty = 0;
        this.totalAmount = BigDecimal.ZERO;
    }

    public ServiceUsed(Long bookingId, Date serviceDate, String service, String category) {
        this();
        this.bookingId = bookingId;
        this.serviceDate = serviceDate;
        this.service = service;
        this.category = category;
    }

    // Helper methods
    public void addServiceItem(ServiceItem item) {
        if (item != null) {
            serviceItems.add(item);
            recalculateTotals();
        }
    }

    public void removeServiceItem(ServiceItem item) {
        if (serviceItems.remove(item)) {
            recalculateTotals();
        }
    }

    public void clearServiceItems() {
        serviceItems.clear();
        recalculateTotals();
    }

    private void recalculateTotals() {
        totalQty = serviceItems.stream().mapToInt(ServiceItem::getQuantity).sum();
        totalAmount = serviceItems.stream()
            .map(ServiceItem::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean hasServiceItems() {
        return !serviceItems.isEmpty();
    }

    public int getServiceItemCount() {
        return serviceItems.size();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }

    public Date getServiceDate() { return serviceDate; }
    public void setServiceDate(Date serviceDate) { this.serviceDate = serviceDate; }

    public String getService() { return service; }
    public void setService(String service) { this.service = service; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTotalQty() { return totalQty; }
    public void setTotalQty(Integer totalQty) { this.totalQty = totalQty; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public List<ServiceItem> getServiceItems() { return serviceItems; }
    public void setServiceItems(List<ServiceItem> serviceItems) { 
        this.serviceItems = serviceItems != null ? serviceItems : new ArrayList<>();
        recalculateTotals();
    }

    @Override
    public String toString() {
        return "ServiceUsed{" +
                "id=" + id +
                ", bookingId=" + bookingId +
                ", serviceDate=" + serviceDate +
                ", service='" + service + '\'' +
                ", category='" + category + '\'' +
                ", totalQty=" + totalQty +
                ", totalAmount=" + totalAmount +
                ", itemCount=" + serviceItems.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceUsed that = (ServiceUsed) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
