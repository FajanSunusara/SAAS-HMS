package application.models;

import java.time.LocalDate;

public class DailyCharges {
    private Long bookingId;
    private Long guestId;
    private String guestName;
    private String roomNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private double dailyRate;
    private double advancePaid;
    private double pendingAmount;
    private String status;
    private int daysStayed;
    private double totalChargesIncurred;
    
    // Constructors
    public DailyCharges() {}
    
    public DailyCharges(Long bookingId, String guestName, String roomNo, 
                       LocalDate checkInDate, double dailyRate) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.roomNo = roomNo;
        this.checkInDate = checkInDate;
        this.dailyRate = dailyRate;
    }
    
    // Getters and Setters
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public Long getGuestId() { return guestId; }
    public void setGuestId(Long guestId) { this.guestId = guestId; }
    
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    
    public double getDailyRate() { return dailyRate; }
    public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }
    
    public double getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(double advancePaid) { this.advancePaid = advancePaid; }
    
    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getDaysStayed() { return daysStayed; }
    public void setDaysStayed(int daysStayed) { this.daysStayed = daysStayed; }
    
    public double getTotalChargesIncurred() { return totalChargesIncurred; }
    public void setTotalChargesIncurred(double totalChargesIncurred) { 
        this.totalChargesIncurred = totalChargesIncurred; 
    }
    
    @Override
    public String toString() {
        return String.format("DailyCharges{bookingId=%d, guest='%s', room='%s', pending=%.2f}", 
                           bookingId, guestName, roomNo, pendingAmount);
    }
}
