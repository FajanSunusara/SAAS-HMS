package com.hotel.reception.repository;

import com.hotel.reception.model.entity.ServiceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceChargeRepository extends JpaRepository<ServiceCharge, Long> {
    
    List<ServiceCharge> findByBookingBookingId(Long bookingId);
    
    List<ServiceCharge> findByGuestGuestId(Long guestId);
    
    List<ServiceCharge> findByBookingBookingIdAndIsPaid(Long bookingId, Boolean isPaid);
    
    // Add these methods to your existing ServiceChargeRepository:
    List<ServiceCharge> findByGuestGuestIdOrderByChargeDateDesc(Long guestId);
    List<ServiceCharge> findTop10ByGuestGuestIdOrderByChargeDateDesc(Long guestId);
    
    @Query("SELECT COALESCE(SUM(sc.amount), 0) FROM ServiceCharge sc WHERE sc.guest.guestId = :guestId")
    Double sumAmountByGuestId(@Param("guestId") Long guestId);
    
    @Query("SELECT COALESCE(SUM(sc.amount), 0) FROM ServiceCharge sc WHERE sc.guest.guestId = :guestId AND sc.isPaid = true")
    Double sumPaidAmountByGuestId(@Param("guestId") Long guestId);
    
    @Query("SELECT COALESCE(SUM(sc.amount), 0) FROM ServiceCharge sc WHERE sc.guest.guestId = :guestId AND sc.isPaid = false")
    Double sumUnpaidAmountByGuestId(@Param("guestId") Long guestId);
}