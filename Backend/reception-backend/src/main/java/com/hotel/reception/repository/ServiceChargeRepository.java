package com.hotel.reception.repository;

import com.hotel.reception.model.entity.ServiceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceChargeRepository extends JpaRepository<ServiceCharge, Long> {
    
    List<ServiceCharge> findByBookingBookingId(Long bookingId);
    
    List<ServiceCharge> findByGuestGuestId(Long guestId);
    
    List<ServiceCharge> findByBookingBookingIdAndIsPaid(Long bookingId, Boolean isPaid);
}