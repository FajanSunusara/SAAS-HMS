package com.hotel.reception.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingFormResponse {
    private Long bookingId;
    private String bookingCode;
    private String bookingType; // SINGLE, GROUP, COMPANY
    private String bookingSource; // WALK_IN, WEBSITE, etc.
    private String status; // CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED
    private String paymentStatus; // PENDING, PARTIAL, PAID
    
    // Guest Information
    private GuestResponse guest;
    private List<GuestResponse> groupMembers;
    
    // Stay Information
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutTime;
    
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private Integer nights;
    private Integer adults;
    private Integer children;
    private Integer infants;
    private String purposeOfVisit;
    private String specialInstructions;
    
    // Room Information
    private List<RoomResponse> rooms;
    private Integer totalRooms;
    
    // Pricing Information
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal extraServicesAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private BigDecimal balanceDue;
    
    // Payment Information
    private String paymentMethod;
    private String transactionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate;
    
    private String paymentRemarks;
    
    // Company Information (for company bookings)
    private String companyName;
    private String companyTaxId;
    
    // Additional Information
    private String specialRequests;
    private String remarks;
    
    // Timestamps
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @Data
    public static class GuestResponse {
        private Long guestId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String nationality;
        private String idType;
        private String idNumber;
        private String loyaltyNumber;
        private String vipStatus;
        private String guestPhotoUrl;
        private String idProofUrl;
    }
    
    @Data
    public static class RoomResponse {
        private String roomNumber;
        private String roomType;
        private Integer floorNumber;
        private BigDecimal rate;
        private String status;
        private List<String> amenities;
        private GuestResponse assignedGuest;
    }
}