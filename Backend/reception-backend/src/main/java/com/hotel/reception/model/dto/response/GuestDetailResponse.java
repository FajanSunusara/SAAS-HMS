package com.hotel.reception.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuestDetailResponse {
    
    // Main guest information
    private GuestInfoResponse guest;
    private IdentityResponse identity;
    private AddressResponse address;
    private PreferencesSection preferences;
    private CurrentStayResponse currentStay;
    private FinancialSummaryResponse financialSummary;
    private BookingHistorySection bookingHistory;
    private PaymentHistorySection paymentHistory;
    private ServiceHistorySection serviceHistory;
    private ActivityTimelineSection activityTimeline;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuestInfoResponse {
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String gender;
        private String dateOfBirth;
        private String nationality;
        private String profilePhoto;
        private String status;
        private String guestId;
        private String memberSince;
        private String loyaltyTier;
        private Integer loyaltyPoints;
        private Long totalStays;
        private String lifetimeValue;
        private String vipLevel;
        private String corporateAccount;
        private String accountManager;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentityResponse {
        private String idType;
        private String idNumber;
        private String issuingAuthority;
        private String issueCountry;
        private String issueDate;
        private String expiryDate;
        private String documentUrl;
        private Boolean verified;
        private String verificationDate;
        private String verifiedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String line1;
        private String line2;
        private String city;
        private String state;
        private String country;
        private String zipCode;
        private String type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentStayResponse {
        private String id;
        private String roomNumber;
        private String roomType;
        private Integer floor;
        private String view;
        private String checkIn;
        private String checkOut;
        private Integer nights;
        private Integer nightsElapsed;
        private Integer nightsRemaining;
        private String ratePlan;
        private BigDecimal ratePerNight;
        private List<String> includes;
        private String bookingSource;
        private String bookedBy;
        private String specialInstructions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialSummaryResponse {
        private BigDecimal roomCharges;
        private BigDecimal serviceCharges;
        private BigDecimal taxes;
        private BigDecimal totalCharges;
        private BigDecimal advancePaid;
        private BigDecimal pendingAmount;
        private BigDecimal creditLimit;
        private BigDecimal availableCredit;
        private String lastPaymentDate;
        private String paymentMethod;
        private String dueDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingHistorySection {
        private List<BookingHistoryItem> bookings;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingHistoryItem {
        private String id;
        private String dates;
        private String roomType;
        private String room;
        private String status;
        private String total;
        private BigDecimal revenue;
        private String source;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistorySection {
        private List<PaymentHistoryItem> payments;
        private Long total;
        private Integer page;
        private Integer size;
        private Integer totalPages;
        private BigDecimal totalPayments;
        private BigDecimal outstandingBalance;
        private Long paymentMethodsUsed;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistoryItem {
        private String date;
        private String transactionId;
        private String amount;
        private String method;
        private String purpose;
        private String invoice;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHistorySection {
        private List<ServiceHistoryItem> services;
        private Long totalServices;
        private Long completed;
        private Long pending;
        private BigDecimal totalCharges;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHistoryItem {
        private String date;
        private String service;
        private String description;
        private String charge;
        private String status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferencesSection {
        // Room preferences
        private String bedType;
        private String pillow;
        private String roomTemp;
        private String floorLevel;
        private String smoking;
        private String newspaper;
        private String amenities;
        private String food;
        private String allergies;
        private String specialRequests;
        private String vipNotes;
        
        // Loyalty info
        private String loyaltyTier;
        private String memberSince;
        private Integer loyaltyPoints;
        private List<String> benefits;
        
        // Corporate info
        private String corporateAccount;
        private String accountManager;
        private String paymentTerms;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityTimelineSection {
        private List<ActivityTimelineItem> activities;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityTimelineItem {
        private String time;
        private String event;
        private String staff;
        private String icon;
        private String type;
    }
}