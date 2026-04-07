package com.hotel.reception.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hotel.reception.model.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "booking_code", unique = true, nullable = false, length = 50)
    private String bookingCode;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id")
    private Guest guest;
    
    @Column(name = "booking_type", length = 20)
    private String bookingType = "SINGLE";
    
    @Column(name = "booking_source", length = 50)
    private String bookingSource = "WALK_IN";
    
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    
    @Column(name = "check_in_time")
    private LocalTime checkInTime = LocalTime.of(14, 0);
    
    @Column(name = "check_out_time")
    private LocalTime checkOutTime = LocalTime.of(12, 0);
    
    @Column(name = "actual_check_in")
    private LocalDateTime actualCheckIn;
    
    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;
    
    @Column(name = "nights", nullable = false)
    private Integer nights;
    
    @Column(name = "adults")
    private Integer adults = 1;
    
    @Column(name = "children")
    private Integer children = 0;
    
    @Column(name = "infants")
    private Integer infants = 0;
    
    @Column(name = "purpose_of_visit", length = 100)
    private String purposeOfVisit;
    
    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30)
    private BookingStatus status = BookingStatus.CONFIRMED;
    
    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "PENDING";
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "manual_discount", precision = 10, scale = 2)
    private BigDecimal manualDiscount = BigDecimal.ZERO;
    
    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage = new BigDecimal("10.00");
    
    @Column(name = "include_tax")
    private Boolean includeTax = true;
    
    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;
    
    // New field: basePrice
    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;
    
    // New field: companyName (for company bookings)
    @Column(name = "company_name", length = 200)
    private String companyName;
    
    // New field: companyTaxId (for company bookings)
    @Column(name = "company_tax_id", length = 50)
    private String companyTaxId;
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingRoom> bookingRooms = new ArrayList<>();
    
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMembers = new ArrayList<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
    
    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;
}