package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByPaymentCode(String paymentCode);
    
    // FIXED: Changed from findByInvoiceId to findByInvoiceInvoiceId
    List<Payment> findByInvoiceInvoiceId(Long invoiceId);
    
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    List<Payment> findPaymentsBetweenDates(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.paymentDate = :date AND p.status = 'COMPLETED'")
    BigDecimal getTotalCollectionByDate(@Param("date") LocalDate date);
    
    @Query("SELECT p.paymentMethod, SUM(p.amountPaid) FROM Payment p " +
           "WHERE p.paymentDate BETWEEN :start AND :end AND p.status = 'COMPLETED' " +
           "GROUP BY p.paymentMethod")
    List<Object[]> getPaymentMethodBreakdown(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    @Query("SELECT SUM(p.amountPaid) FROM Payment p WHERE p.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalRevenueInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    // FIXED: Uncommented and kept these methods as they are needed
    List<Payment> findByBookingBookingId(Long bookingId);
    
    List<Payment> findByGuestGuestId(Long guestId);
    
    // FIXED: Added @Query annotation for findByInvoiceId method
    @Query("SELECT p FROM Payment p WHERE p.invoice.invoiceId = :invoiceId")
    List<Payment> findByInvoiceId(@Param("invoiceId") Long invoiceId);
    
    @Query("SELECT p FROM Payment p WHERE DATE(p.paymentDate) = :date")
    List<Payment> findByPaymentDate(@Param("date") LocalDate date);
    
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :start AND :end")
    List<Payment> findByPaymentDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.booking.bookingId = :bookingId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidByBookingId(@Param("bookingId") Long bookingId);
    
    Page<Payment> findByPaymentMethod(String paymentMethod, Pageable pageable);
    
    Page<Payment> findByPaymentDateBetween(LocalDate start, LocalDate end, Pageable pageable);
    
    // Additional useful methods
    @Query("SELECT p FROM Payment p WHERE p.status = :status")
    List<Payment> findByStatus(@Param("status") String status);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.bookingId = :bookingId AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByBookingId(@Param("bookingId") Long bookingId);
    
    @Query("SELECT p FROM Payment p WHERE p.guest.guestId = :guestId AND p.status = 'COMPLETED'")
    List<Payment> findCompletedPaymentsByGuestId(@Param("guestId") Long guestId);
    
    @Query("SELECT COALESCE(SUM(p.amountPaid), 0) FROM Payment p WHERE p.invoice.invoiceId = :invoiceId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidByInvoiceId(@Param("invoiceId") Long invoiceId);
}