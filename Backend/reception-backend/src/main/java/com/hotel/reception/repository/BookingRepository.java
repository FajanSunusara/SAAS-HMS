package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingCode(String bookingCode);
    
    List<Booking> findByGuestGuestId(Long guestId);
    
    List<Booking> findByStatus(String status);
    
    List<Booking> findByBookingSource(String bookingSource);
    
    List<Booking> findByCheckInDateBetween(LocalDate start, LocalDate end);
    
    List<Booking> findByCheckOutDateBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT b FROM Booking b WHERE b.checkInDate <= :date AND b.checkOutDate >= :date")
    List<Booking> findActiveBookingsOnDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkInDate = :date")
    Long countCheckInsToday(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkOutDate = :date")
    Long countCheckOutsToday(@Param("date") LocalDate date);
}