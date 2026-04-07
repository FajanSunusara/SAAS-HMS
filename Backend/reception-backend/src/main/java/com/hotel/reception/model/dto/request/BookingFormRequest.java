package com.hotel.reception.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class BookingFormRequest {
    
    // Guest Information
    @NotBlank(message = "First name is required")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    private String lastName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    @NotBlank(message = "Nationality is required")
    private String nationality;
    
    private String address;
    private String idType;
    private String idNumber;
    private String contactType = "same";
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;
    
    private String gender;
    private String passportNumber;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate passportExpiry;
    
    private String country;
    private String state;
    private String city;
    private String zipCode;
    private String emergencyContact;
    private String company;
    private String businessEmail;
    private String loyaltyNumber;
    
    // Stay Details
    @NotNull(message = "Check-in date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;
    
    @Min(value = 1, message = "At least 1 adult is required")
    private Integer adults = 1;
    
    @Min(value = 0)
    private Integer children = 0;
    
    @Min(value = 0)
    private Integer infants = 0;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime arrivalTime = LocalTime.of(14, 0);
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime departureTime = LocalTime.of(12, 0);
    
    private String purposeOfVisit;
    private String specialInstructions;
    
    // Room Selection
    private String roomSelectionMode = "type";
    private List<RoomTypeSelection> selectedRoomTypes;
    private List<String> selectedRooms;
    private Integer totalRooms = 0;
    
    // Pricing & Offers
    private String ratePlan;
    private BigDecimal basePrice = BigDecimal.ZERO;
    private String discountType = "percentage";
    private BigDecimal discountValue = BigDecimal.ZERO;
    private String promoCode;
    private BigDecimal taxPercentage = new BigDecimal("10");
    private List<Long> extraServices;
    
    // Payment
    private String paymentType = "partial";
    private String paymentMethod;
    private BigDecimal advancePayment = BigDecimal.ZERO;
    private String transactionId;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate paymentDate = LocalDate.now();
    
    private String paymentRemarks;
    
    // Additional Information
    private String specialRequests;
    private String bookingSource = "walk-in";
    private String remarks;
    private Boolean marketingOptIn = false;
    
    @AssertTrue(message = "You must accept the terms and conditions")
    private Boolean termsAccepted = false;
    
    // Room Preferences
    private String bedPreference;
    private String smokingPreference = "non-smoking";
    private String floorPreference;
    private String viewPreference;
    
    // Special Services
    private Boolean extraBed = false;
    private Boolean crib = false;
    private Boolean wheelchairAccess = false;
    private Boolean earlyCheckIn = false;
    private Boolean lateCheckOut = false;
    
    // 🟢 NEW FIELDS
    private String bookingType;          // SINGLE, GROUP, COMPANY
    private String companyName;          // for company bookings
    private String companyTaxId;         // for company bookings
    private List<GuestRequest> groupMembers;  // for group bookings
    
    @Data
    public static class RoomTypeSelection {
        private Long typeId;
        private Integer numberOfRooms;
    }
}