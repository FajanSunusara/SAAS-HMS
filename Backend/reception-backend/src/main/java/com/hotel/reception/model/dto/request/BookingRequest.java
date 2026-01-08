package com.hotel.reception.model.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    
    // Guest information (for new guests or guest ID for existing)
    private Long guestId;
    private GuestRequest guestDetails;
    
    @NotBlank(message = "Booking type is required")
    private String bookingType; // SINGLE, GROUP, COMPANY
    
    private String bookingSource; // WALK_IN, PHONE, WEBSITE, OTA
    
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or future")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in future")
    private LocalDate checkOutDate;
    
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    
    @Positive(message = "Number of adults must be positive")
    private Integer adults;
    
    private Integer children;
    private Integer infants;
    
    // Room selection
    @NotNull(message = "At least one room must be selected")
    private List<Long> roomIds;
    
    // Group booking
    private List<GuestRequest> groupMembers;
    
    // Preferences
    private RoomPreferenceRequest preferences;
    
    // Additional info
    private String purposeOfVisit;
    private String specialInstructions;
    
    // Pricing
    private BigDecimal discountPercentage;
    private BigDecimal manualDiscount;
    private String discountCode;
    private BigDecimal taxPercentage;
    private Boolean includeTax;
    
    // Services
    private List<Long> extraServiceIds;
    
    // Payment (optional for advance payment)
    private PaymentRequest advancePayment;
    
    private Boolean termsAccepted;
}
