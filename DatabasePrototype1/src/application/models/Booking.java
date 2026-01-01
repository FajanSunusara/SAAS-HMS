package application.models;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class Booking {
    private Long id;
    private Long reservationId;
    private Long guestId;
    private String roomNo;
    private String roomType; // UI display
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalAmount;
    private BigDecimal advancePaid;
    private String paymentStatus;
    private String status;
    private String guestName; // UI display
    private Timestamp createdAt;
    
    // NEW FIELDS to match database schema
    private String nationality;
    private BigDecimal ratePerNight;
    private String documentLink;
    private BigDecimal gstRate;
    private BigDecimal baseAmount;
    private BigDecimal gstAmount;
    private Boolean gstIncluded;

    // Constructors
    public Booking() {}

    public Booking(Long guestId, String roomNo, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guestId = guestId;
        this.roomNo = roomNo;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    // For DashboardPopup upcoming/cancelled bookings
    public Booking(String guestName, String checkInDate, String roomType, String roomNo) {
        this.guestName = guestName;
        this.checkInDate = LocalDate.parse(checkInDate);
        this.roomType = roomType;
        this.roomNo = roomNo;
    }

    // Existing getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }
    
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(BigDecimal advancePaid) { this.advancePaid = advancePaid; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // NEW FIELD getters and setters
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    
    public BigDecimal getRatePerNight() { return ratePerNight; }
    public void setRatePerNight(BigDecimal ratePerNight) { this.ratePerNight = ratePerNight; }
    
    public String getDocumentLink() { return documentLink; }
    public void setDocumentLink(String documentLink) { this.documentLink = documentLink; }
    
    public BigDecimal getGstRate() { return gstRate; }
    public void setGstRate(BigDecimal gstRate) { this.gstRate = gstRate; }
    
    public BigDecimal getBaseAmount() { return baseAmount; }
    public void setBaseAmount(BigDecimal baseAmount) { this.baseAmount = baseAmount; }
    
    public BigDecimal getGstAmount() { return gstAmount; }
    public void setGstAmount(BigDecimal gstAmount) { this.gstAmount = gstAmount; }
    
    public Boolean getGstIncluded() { return gstIncluded; }
    public void setGstIncluded(Boolean gstIncluded) { this.gstIncluded = gstIncluded; }
    
    

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", guestId=" + guestId +
                ", roomNo='" + roomNo + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalAmount=" + totalAmount +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", status='" + status + '\'' +
                ", nationality='" + nationality + '\'' +
                ", ratePerNight=" + ratePerNight +
                ", gstRate=" + gstRate +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Booking)) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
