package application.models;

import java.time.LocalDate;

public class GuestBookingPayment {
    // Guest
    private String guestName;
    private String phone;
    private String email;
    private String address;
    private String guestNationality;
    private String gstNumber;
    private String idType;
    private String idNumber;

    // Booking
    private int bookingId;
    private String roomNo;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double ratePerNight;
    private double baseAmount;
    private double gstRate;
    private double gstAmount;
    private boolean gstIncluded;
    private double totalAmount;
    private double advancePaid;
    private String paymentStatus;
    private String bookingStatus;
    private String documentLink;
    private String bookingNationality;

    // Constructor with all fields
    public GuestBookingPayment(int bookingId, String guestName, String phone, String email, String address, String guestNationality, String gstNumber, String idType, String idNumber, String roomNo, LocalDate checkIn, LocalDate checkOut, double ratePerNight, double baseAmount, double gstRate, double gstAmount, boolean gstIncluded, double totalAmount, double advancePaid, String paymentStatus, String bookingStatus, String documentLink, String bookingNationality) {
        this.bookingId = bookingId;
        this.guestName = guestName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.guestNationality = guestNationality;
        this.gstNumber = gstNumber;
        this.idType = idType;
        this.idNumber = idNumber;
        this.roomNo = roomNo;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.ratePerNight = ratePerNight;
        this.baseAmount = baseAmount;
        this.gstRate = gstRate;
        this.gstAmount = gstAmount;
        this.gstIncluded = gstIncluded;
        this.totalAmount = totalAmount;
        this.advancePaid = advancePaid;
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
        this.documentLink = documentLink;
        this.bookingNationality = bookingNationality;
    }

    // Getters
    public int getBookingId() {
        return bookingId;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getGuestNationality() {
        return guestNationality;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public String getIdType() {
        return idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public String getRoomNo() {
        return roomNo;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public double getRatePerNight() {
        return ratePerNight;
    }

    public double getBaseAmount() {
        return baseAmount;
    }

    public double getGstRate() {
        return gstRate;
    }

    public double getGstAmount() {
        return gstAmount;
    }

    public boolean isGstIncluded() {
        return gstIncluded;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public double getAdvancePaid() {
        return advancePaid;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public String getDocumentLink() {
        return documentLink;
    }

    public String getBookingNationality() {
        return bookingNationality;
    }

    // Setters
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGuestNationality(String guestNationality) {
        this.guestNationality = guestNationality;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setRoomNo(String roomNo) {
        this.roomNo = roomNo;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public void setRatePerNight(double ratePerNight) {
        this.ratePerNight = ratePerNight;
    }

    public void setBaseAmount(double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public void setGstRate(double gstRate) {
        this.gstRate = gstRate;
    }

    public void setGstAmount(double gstAmount) {
        this.gstAmount = gstAmount;
    }

    public void setGstIncluded(boolean gstIncluded) {
        this.gstIncluded = gstIncluded;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setAdvancePaid(double advancePaid) {
        this.advancePaid = advancePaid;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setDocumentLink(String documentLink) {
        this.documentLink = documentLink;
    }

    public void setBookingNationality(String bookingNationality) {
        this.bookingNationality = bookingNationality;
    }
}