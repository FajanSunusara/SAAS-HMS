package application.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Enhanced BookingData model to support comprehensive invoice functionality
 * This model can be used universally for any hotel service type
 */
public class BookingData {
    
    // Basic Invoice Information
    private String invoiceNumber;
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private String invoiceType = "Tax Invoice";
    
    // Hotel Information
    private String hotelName;
    private String hotelAddress;
    private String hotelPhone;
    private String hotelEmail;
    private String hotelWebsite;
    private String hotelGstNumber;
    
    // Guest Information
    private String guestName;
    private String mobileNumber;
    private String guestEmail;
    private String guestAddress;
    private String guestGst;
    private String guestCity;
    private String guestState;
    private String guestCountry;
    private int guestCount = 1;
    
    // Service Information (Universal for all hotel services)
    private String serviceType = "Accommodation"; // Accommodation, Restaurant, Spa, Conference, Events, etc.
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfNights;
    private String serviceLocation; // Room, Restaurant, Spa, Conference Hall, etc.
    private String serviceDuration; // Nights, Hours, Days, etc.
    private double serviceRate; // Rate per unit (night, hour, etc.)
    
    // Financial Information
    private double roomCharges;
    private double serviceCharges;
    private double amenityCharges;
    private double restaurantCharges;
    private double laundryCharges;
    private double miscellaneousCharges;
    private double discount;
    private double subtotal;
    private double gst;
    private double cgst;
    private double sgst;
    private double igst; // For inter-state transactions
    private double totalPayable;
    private double advancePaid;
    private double balanceDue;
    
    // Payment Information
    private String paymentMethod;
    private String transactionId;
    private String paymentStatus = "Pending";
    private LocalDate paymentDate;
    private String paymentReference;
    
    // Service Items and Payment History
    private List<ServiceItem> serviceItems = new ArrayList<>();
    private List<PaymentRecord> paymentHistory = new ArrayList<>();
    
    // Terms and Customization
    private String customTerms;
    private String specialInstructions;
    private String cancellationPolicy;
    private double cancellationCharges;
    
    // Constructors
    public BookingData() {}
    
    public BookingData(String guestName, String roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        this.guestName = guestName;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        calculateNumberOfNights();
    }
    
    // Utility Methods
    public void calculateNumberOfNights() {
        if (checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate)) {
            this.numberOfNights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        } else {
            this.numberOfNights = 0;
        }
    }
    
    public void calculateTotals() {
        // Calculate subtotal from all charges minus discount
        this.subtotal = Math.max(0, roomCharges + serviceCharges + amenityCharges + 
                                restaurantCharges + laundryCharges + miscellaneousCharges - discount);
        
        // Calculate GST components (18% total - can be split as CGST 9% + SGST 9% or IGST 18%)
        this.gst = subtotal * 0.18;
        this.cgst = gst / 2; // 9%
        this.sgst = gst / 2; // 9%
        
        // Calculate total payable
        this.totalPayable = subtotal + gst;
        
        // Calculate balance due
        this.balanceDue = Math.max(0, totalPayable - advancePaid);
        
        // Update payment status
        updatePaymentStatus();
    }
    
    public void updatePaymentStatus() {
        if (balanceDue <= 0.01) { // Accounting for floating point precision
            this.paymentStatus = "Fully Paid";
        } else if (advancePaid > 0) {
            this.paymentStatus = "Partially Paid";
        } else {
            this.paymentStatus = "Pending";
        }
    }
    
    public void addServiceItem(ServiceItem item) {
        if (serviceItems == null) {
            serviceItems = new ArrayList<>();
        }
        serviceItems.add(item);
    }
    
    public void addPaymentRecord(PaymentRecord record) {
        if (paymentHistory == null) {
            paymentHistory = new ArrayList<>();
        }
        paymentHistory.add(record);
    }
    
    // Getters and Setters
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    
    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public String getInvoiceType() { return invoiceType; }
    public void setInvoiceType(String invoiceType) { this.invoiceType = invoiceType; }
    
    // Hotel Information
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }
    
    public String getHotelAddress() { return hotelAddress; }
    public void setHotelAddress(String hotelAddress) { this.hotelAddress = hotelAddress; }
    
    public String getHotelPhone() { return hotelPhone; }
    public void setHotelPhone(String hotelPhone) { this.hotelPhone = hotelPhone; }
    
    public String getHotelEmail() { return hotelEmail; }
    public void setHotelEmail(String hotelEmail) { this.hotelEmail = hotelEmail; }
    
    public String getHotelWebsite() { return hotelWebsite; }
    public void setHotelWebsite(String hotelWebsite) { this.hotelWebsite = hotelWebsite; }
    
    public String getHotelGstNumber() { return hotelGstNumber; }
    public void setHotelGstNumber(String hotelGstNumber) { this.hotelGstNumber = hotelGstNumber; }
    
    // Guest Information
    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }
    
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    
    public String getGuestAddress() { return guestAddress; }
    public void setGuestAddress(String guestAddress) { this.guestAddress = guestAddress; }
    
    public String getGuestGst() { return guestGst; }
    public void setGuestGst(String guestGst) { this.guestGst = guestGst; }
    
    public String getGuestCity() { return guestCity; }
    public void setGuestCity(String guestCity) { this.guestCity = guestCity; }
    
    public String getGuestState() { return guestState; }
    public void setGuestState(String guestState) { this.guestState = guestState; }
    
    public String getGuestCountry() { return guestCountry; }
    public void setGuestCountry(String guestCountry) { this.guestCountry = guestCountry; }
    
    public int getGuestCount() { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }
    
    // Service Information
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    
    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { 
        this.checkInDate = checkInDate; 
        calculateNumberOfNights();
    }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { 
        this.checkOutDate = checkOutDate; 
        calculateNumberOfNights();
    }
    
    public int getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(int numberOfNights) { this.numberOfNights = numberOfNights; }
    
    public String getServiceLocation() { return serviceLocation; }
    public void setServiceLocation(String serviceLocation) { this.serviceLocation = serviceLocation; }
    
    public String getServiceDuration() { return serviceDuration; }
    public void setServiceDuration(String serviceDuration) { this.serviceDuration = serviceDuration; }
    
    public double getServiceRate() { return serviceRate; }
    public void setServiceRate(double serviceRate) { this.serviceRate = serviceRate; }
    
    // Financial Information
    public double getRoomCharges() { return roomCharges; }
    public void setRoomCharges(double roomCharges) { this.roomCharges = roomCharges; }
    
    public double getServiceCharges() { return serviceCharges; }
    public void setServiceCharges(double serviceCharges) { this.serviceCharges = serviceCharges; }
    
    public double getAmenityCharges() { return amenityCharges; }
    public void setAmenityCharges(double amenityCharges) { this.amenityCharges = amenityCharges; }
    
    public double getRestaurantCharges() { return restaurantCharges; }
    public void setRestaurantCharges(double restaurantCharges) { this.restaurantCharges = restaurantCharges; }
    
    public double getLaundryCharges() { return laundryCharges; }
    public void setLaundryCharges(double laundryCharges) { this.laundryCharges = laundryCharges; }
    
    public double getMiscellaneousCharges() { return miscellaneousCharges; }
    public void setMiscellaneousCharges(double miscellaneousCharges) { this.miscellaneousCharges = miscellaneousCharges; }
    
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    
    public double getGst() { return gst; }
    public void setGst(double gst) { this.gst = gst; }
    
    public double getCgst() { return cgst; }
    public void setCgst(double cgst) { this.cgst = cgst; }
    
    public double getSgst() { return sgst; }
    public void setSgst(double sgst) { this.sgst = sgst; }
    
    public double getIgst() { return igst; }
    public void setIgst(double igst) { this.igst = igst; }
    
    public double getTotalPayable() { return totalPayable; }
    public void setTotalPayable(double totalPayable) { this.totalPayable = totalPayable; }
    
    public double getAdvancePaid() { return advancePaid; }
    public void setAdvancePaid(double advancePaid) { this.advancePaid = advancePaid; }
    
    public double getBalanceDue() { return balanceDue; }
    public void setBalanceDue(double balanceDue) { this.balanceDue = balanceDue; }
    
    // Payment Information
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
    
    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    
    // Service Items and Payment History
    public List<ServiceItem> getServiceItems() { return serviceItems; }
    public void setServiceItems(List<ServiceItem> serviceItems) { this.serviceItems = serviceItems; }
    
    public List<PaymentRecord> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(List<PaymentRecord> paymentHistory) { this.paymentHistory = paymentHistory; }
    
    // Terms and Customization
    public String getCustomTerms() { return customTerms; }
    public void setCustomTerms(String customTerms) { this.customTerms = customTerms; }
    
    public String getSpecialInstructions() { return specialInstructions; }
    public void setSpecialInstructions(String specialInstructions) { this.specialInstructions = specialInstructions; }
    
    public String getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(String cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
    
    public double getCancellationCharges() { return cancellationCharges; }
    public void setCancellationCharges(double cancellationCharges) { this.cancellationCharges = cancellationCharges; }
    
    @Override
    public String toString() {
        return String.format("BookingData{invoiceNumber='%s', guestName='%s', serviceType='%s', totalPayable=%.2f, paymentStatus='%s'}", 
            invoiceNumber, guestName, serviceType, totalPayable, paymentStatus);
    }
    
    // Static factory methods for different service types
    public static BookingData createAccommodationInvoice(String guestName, String roomNumber, 
                                                       LocalDate checkIn, LocalDate checkOut, 
                                                       double roomRate) {
        BookingData booking = new BookingData(guestName, roomNumber, checkIn, checkOut);
        booking.setServiceType("Accommodation");
        booking.setRoomCharges(roomRate * booking.getNumberOfNights());
        booking.calculateTotals();
        return booking;
    }
    
    public static BookingData createRestaurantInvoice(String guestName, double billAmount) {
        BookingData booking = new BookingData();
        booking.setGuestName(guestName);
        booking.setServiceType("Restaurant");
        booking.setRestaurantCharges(billAmount);
        booking.setServiceLocation("Restaurant");
        booking.setInvoiceDate(LocalDate.now());
        booking.calculateTotals();
        return booking;
    }
    
    public static BookingData createSpaInvoice(String guestName, String serviceDescription, 
                                             double serviceAmount, String duration) {
        BookingData booking = new BookingData();
        booking.setGuestName(guestName);
        booking.setServiceType("Spa Services");
        booking.setServiceCharges(serviceAmount);
        booking.setServiceLocation("Spa");
        booking.setServiceDuration(duration);
        booking.setInvoiceDate(LocalDate.now());
        booking.calculateTotals();
        return booking;
    }
    
    public static BookingData createConferenceInvoice(String guestName, String hallName, 
                                                    LocalDate eventDate, double hallCharges, 
                                                    String duration) {
        BookingData booking = new BookingData();
        booking.setGuestName(guestName);
        booking.setServiceType("Conference Room");
        booking.setServiceCharges(hallCharges);
        booking.setServiceLocation(hallName);
        booking.setCheckInDate(eventDate);
        booking.setServiceDuration(duration);
        booking.setInvoiceDate(LocalDate.now());
        booking.calculateTotals();
        return booking;
    }
    
    public static BookingData createEventInvoice(String guestName, String eventType, 
                                               LocalDate eventDate, double eventCharges) {
        BookingData booking = new BookingData();
        booking.setGuestName(guestName);
        booking.setServiceType("Event Services");
        booking.setServiceCharges(eventCharges);
        booking.setCheckInDate(eventDate);
        booking.setInvoiceDate(LocalDate.now());
        booking.calculateTotals();
        return booking;
    }
}