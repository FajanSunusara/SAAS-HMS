package com.hotel.reception.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
    
    public Long getBookingId() {
		return bookingId;
	}

	public void setBookingId(Long bookingId) {
		this.bookingId = bookingId;
	}

	public String getBookingCode() {
		return bookingCode;
	}

	public void setBookingCode(String bookingCode) {
		this.bookingCode = bookingCode;
	}

	public Guest getGuest() {
		return guest;
	}

	public void setGuest(Guest guest) {
		this.guest = guest;
	}

	public String getBookingType() {
		return bookingType;
	}

	public void setBookingType(String bookingType) {
		this.bookingType = bookingType;
	}

	public String getBookingSource() {
		return bookingSource;
	}

	public void setBookingSource(String bookingSource) {
		this.bookingSource = bookingSource;
	}

	public LocalDate getCheckInDate() {
		return checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public LocalTime getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(LocalTime checkInTime) {
		this.checkInTime = checkInTime;
	}

	public LocalTime getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(LocalTime checkOutTime) {
		this.checkOutTime = checkOutTime;
	}

	public LocalDateTime getActualCheckIn() {
		return actualCheckIn;
	}

	public void setActualCheckIn(LocalDateTime actualCheckIn) {
		this.actualCheckIn = actualCheckIn;
	}

	public LocalDateTime getActualCheckOut() {
		return actualCheckOut;
	}

	public void setActualCheckOut(LocalDateTime actualCheckOut) {
		this.actualCheckOut = actualCheckOut;
	}

	public Integer getNights() {
		return nights;
	}

	public void setNights(Integer nights) {
		this.nights = nights;
	}

	public Integer getAdults() {
		return adults;
	}

	public void setAdults(Integer adults) {
		this.adults = adults;
	}

	public Integer getChildren() {
		return children;
	}

	public void setChildren(Integer children) {
		this.children = children;
	}

	public Integer getInfants() {
		return infants;
	}

	public void setInfants(Integer infants) {
		this.infants = infants;
	}

	public String getPurposeOfVisit() {
		return purposeOfVisit;
	}

	public void setPurposeOfVisit(String purposeOfVisit) {
		this.purposeOfVisit = purposeOfVisit;
	}

	public String getSpecialInstructions() {
		return specialInstructions;
	}

	public void setSpecialInstructions(String specialInstructions) {
		this.specialInstructions = specialInstructions;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getDiscountCode() {
		return discountCode;
	}

	public void setDiscountCode(String discountCode) {
		this.discountCode = discountCode;
	}

	public BigDecimal getDiscountPercentage() {
		return discountPercentage;
	}

	public void setDiscountPercentage(BigDecimal discountPercentage) {
		this.discountPercentage = discountPercentage;
	}

	public BigDecimal getManualDiscount() {
		return manualDiscount;
	}

	public void setManualDiscount(BigDecimal manualDiscount) {
		this.manualDiscount = manualDiscount;
	}

	public BigDecimal getTaxPercentage() {
		return taxPercentage;
	}

	public void setTaxPercentage(BigDecimal taxPercentage) {
		this.taxPercentage = taxPercentage;
	}

	public Boolean getIncludeTax() {
		return includeTax;
	}

	public void setIncludeTax(Boolean includeTax) {
		this.includeTax = includeTax;
	}

	public String getReferenceSource() {
		return referenceSource;
	}

	public void setReferenceSource(String referenceSource) {
		this.referenceSource = referenceSource;
	}

	public Boolean getTermsAccepted() {
		return termsAccepted;
	}

	public void setTermsAccepted(Boolean termsAccepted) {
		this.termsAccepted = termsAccepted;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public LocalDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(LocalDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}

	public void setCancellationReason(String cancellationReason) {
		this.cancellationReason = cancellationReason;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "booking_code", unique = true, nullable = false, length = 50)
    private String bookingCode;
    
    @ManyToOne
    @JoinColumn(name = "guest_id", nullable = false)
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
    private LocalTime checkInTime = LocalTime.of(14, 0); // 2:00 PM
    
    @Column(name = "check_out_time")
    private LocalTime checkOutTime = LocalTime.of(12, 0); // 12:00 PM
    
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
    
    @Column(name = "status", length = 30)
    private String status = "CONFIRMED";
    
    @Column(name = "payment_status", length = 30)
    private String paymentStatus = "PENDING";
    
    @Column(name = "discount_code", length = 50)
    private String discountCode;
    
    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage = BigDecimal.ZERO;
    
    @Column(name = "manual_discount", precision = 10, scale = 2)
    private BigDecimal manualDiscount = BigDecimal.ZERO;
    
    @Column(name = "tax_percentage", precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.valueOf(10);
    
    @Column(name = "include_tax")
    private Boolean includeTax = true;
    
    @Column(name = "reference_source", length = 100)
    private String referenceSource;
    
    @Column(name = "terms_accepted")
    private Boolean termsAccepted = false;
    
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