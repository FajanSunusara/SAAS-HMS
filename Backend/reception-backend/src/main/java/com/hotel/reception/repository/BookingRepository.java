package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.enums.BookingStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {
    
    Optional<Booking> findByBookingCode(String bookingCode);
    
    List<Booking> findByGuestGuestId(Long guestId);
    
    List<Booking> findByStatus(String status);
    
    Page<Booking> findByStatus(String status, Pageable pageable);
    
    List<Booking> findByCheckInDate(LocalDate checkInDate);
    
    List<Booking> findByCheckOutDate(LocalDate checkOutDate);
    
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :date AND b.status = 'CONFIRMED'")
    List<Booking> findTodayCheckIns(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :date AND b.status = 'CHECKED_IN'")
    List<Booking> findTodayCheckOuts(@Param("date") LocalDate date);
    
    @Query("SELECT b FROM Booking b WHERE b.checkInDate BETWEEN :start AND :end")
    List<Booking> findBookingsBetweenDates(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );
    
    @Query("SELECT b FROM Booking b WHERE DATE(b.checkInDate) = :date AND b.status != :status")
    List<Booking> findByCheckInDateAndStatusNot(
        @Param("date") LocalDate date, 
        @Param("status") BookingStatus status
    );
    
    @Query("SELECT b FROM Booking b WHERE " +
           "b.checkInDate <= :date AND b.checkOutDate >= :date AND b.status = 'CHECKED_IN'")
    List<Booking> findOccupiedRoomsByDate(@Param("date") LocalDate date);
    
    Long countByStatus(String status);
    
    Long countByCheckInDateAndStatus(LocalDate date, String status);
    
    Long countByCheckOutDateAndStatus(LocalDate date, String status);
    
    
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end")
    Long countBookingsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.guest.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.guest.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "b.bookingCode LIKE CONCAT('%', :keyword, '%')")
    Page<Booking> searchBookings(@Param("keyword") String keyword, Pageable pageable);
    
    Optional<Booking> findFirstByGuestGuestIdAndStatus(Long guestId, String status);
    Long countByGuestGuestIdAndStatus(Long guestId, String status);
    Page<Booking> findByGuestGuestId(Long guestId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.guest.guestId = :guestId AND b.checkInDate <= :date AND b.checkOutDate >= :date AND b.status = 'CHECKED_IN'")
    Optional<Booking> findCurrentStay(@Param("guestId") Long guestId, @Param("date") LocalDate date);
    
    List<Booking> findByGuestGuestIdAndStatusOrderByCheckInDateDesc(Long guestId, String status);
    List<Booking> findTop10ByGuestGuestIdOrderByCheckInDateDesc(Long guestId);
    
    
}
