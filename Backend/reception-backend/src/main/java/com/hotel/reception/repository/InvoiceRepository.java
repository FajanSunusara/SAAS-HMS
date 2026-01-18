package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    
    Optional<Invoice> findByBookingBookingId(Long bookingId);
    
    List<Invoice> findByGuestGuestId(Long guestId);
    
    List<Invoice> findByStatus(String status);
    
    Page<Invoice> findByStatus(String status, Pageable pageable);
    
    List<Invoice> findByIssueDateBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT i FROM Invoice i WHERE i.balanceDue > 0 AND i.status != 'CANCELLED'")
    List<Invoice> findPendingInvoices();
    
    @Query("SELECT SUM(i.totalAmount) FROM Invoice i WHERE i.issueDate BETWEEN :start AND :end")
    BigDecimal getTotalRevenueByDateRange(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    @Query("SELECT SUM(i.balanceDue) FROM Invoice i WHERE i.status IN ('PENDING', 'PARTIAL')")
    BigDecimal getTotalPendingAmount();
    
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.guest.guestId = :guestId")
    Double sumTotalAmountByGuestId(@Param("guestId") Long guestId);
    
    @Query("SELECT COALESCE(SUM(i.balanceDue), 0) FROM Invoice i WHERE i.guest.guestId = :guestId AND i.status = :status")
    Double sumBalanceDueByGuestIdAndStatus(@Param("guestId") Long guestId, @Param("status") String status);
    
//    List<Invoice> findByGuestGuestId(Long guestId);
    List<Invoice> findByGuestGuestIdAndStatus(Long guestId, String status);
    
    @Query("SELECT COALESCE(SUM(i.amountPaid), 0) FROM Invoice i WHERE i.guest.guestId = :guestId")
    Double sumAmountPaidByGuestId(@Param("guestId") Long guestId);
    
    
// // Add to InvoiceRepository.java
//    @Query("SELECT i FROM Invoice i JOIN FETCH i.booking b JOIN FETCH i.guest g WHERE i.invoiceId = :id")
//    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);

//    @Query("SELECT i FROM Invoice i JOIN FETCH i.booking b JOIN FETCH i.guest g WHERE i.invoiceNumber = :invoiceNumber")
//    Optional<Invoice> findByInvoiceNumberWithDetails(@Param("invoiceNumber") String invoiceNumber);
//    
    
    // Add JOIN FETCH queries to avoid N+1 problem
    @Query("SELECT DISTINCT i FROM Invoice i " +
           "LEFT JOIN FETCH i.booking b " +
           "LEFT JOIN FETCH b.bookingRooms br " +
           "LEFT JOIN FETCH br.room " +
           "LEFT JOIN FETCH i.guest g " +
           "LEFT JOIN FETCH i.payments p")
    Page<Invoice> findAllWithDetails(Pageable pageable);
    
    @Query("SELECT DISTINCT i FROM Invoice i " +
           "LEFT JOIN FETCH i.booking b " +
           "LEFT JOIN FETCH b.bookingRooms br " +
           "LEFT JOIN FETCH br.room " +
           "LEFT JOIN FETCH i.guest g " +
           "LEFT JOIN FETCH i.payments p " +
           "WHERE i.invoiceId = :id")
    Optional<Invoice> findByIdWithDetails(@Param("id") Long id);
    
    @Query("SELECT DISTINCT i FROM Invoice i " +
           "LEFT JOIN FETCH i.booking b " +
           "LEFT JOIN FETCH b.bookingRooms br " +
           "LEFT JOIN FETCH br.room " +
           "LEFT JOIN FETCH i.guest g " +
           "LEFT JOIN FETCH i.payments p " +
           "WHERE i.invoiceNumber = :invoiceNumber")
    Optional<Invoice> findByInvoiceNumberWithDetails(@Param("invoiceNumber") String invoiceNumber);
}
