package application.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class RoomBooking {
    private String roomNumber;
    private String roomType;
    private BigDecimal ratePerNight;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int nights;
    private BigDecimal totalAmount;
    private String primaryGuest;
    private String status = "Confirmed";

    // Default constructor
    public RoomBooking() {}

    // Full constructor
    public RoomBooking(String roomNumber, String roomType, BigDecimal ratePerNight, 
                      LocalDate checkInDate, LocalDate checkOutDate, String primaryGuest) {
        this.roomNumber = roomNumber;
        this.roomType = roomType;
        this.ratePerNight = ratePerNight;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.primaryGuest = primaryGuest;
        calculateNightsAndTotal();
    }

    private void calculateNightsAndTotal() {
        if (checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate)) {
            this.nights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            this.totalAmount = ratePerNight.multiply(BigDecimal.valueOf(nights));
        } else {
            this.nights = 0;
            this.totalAmount = BigDecimal.ZERO;
        }
    }

    // Getters and Setters
    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public BigDecimal getRatePerNight() {
        return ratePerNight;
    }

    public void setRatePerNight(BigDecimal ratePerNight) {
        this.ratePerNight = ratePerNight;
        calculateNightsAndTotal();
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
        calculateNightsAndTotal();
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
        calculateNightsAndTotal();
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPrimaryGuest() {
        return primaryGuest;
    }

    public void setPrimaryGuest(String primaryGuest) {
        this.primaryGuest = primaryGuest;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // ADD THESE MISSING METHODS:
    
    /**
     * Alias for getPrimaryGuest() - returns the guest name
     */
    public String getGuestName() {
        return primaryGuest;
    }

    /**
     * Alias for getRatePerNight() - returns the rate per night as BigDecimal
     */
    public BigDecimal getRatePerNightValue() {
        return ratePerNight;
    }

    /**
     * Returns formatted rate per night as string with currency symbol
     */
    public String getRatePerNightFormatted() {
        return "₹" + ratePerNight.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * Returns formatted total amount as string with currency symbol
     */
    public String getTotalAmountFormatted() {
        return "₹" + totalAmount.setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoomBooking that = (RoomBooking) o;
        return Objects.equals(roomNumber, that.roomNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }

    @Override
    public String toString() {
        return "RoomBooking{" +
                "roomNumber='" + roomNumber + '\'' +
                ", roomType='" + roomType + '\'' +
                ", ratePerNight=" + ratePerNight +
                ", nights=" + nights +
                ", totalAmount=" + totalAmount +
                ", primaryGuest='" + primaryGuest + '\'' +
                '}';
    }
}