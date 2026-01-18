package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ReservationDashboardRepository extends JpaRepository<Booking, Long> {
    
    // Calendar view queries
    @Query("SELECT r FROM Room r WHERE " +
           "(:roomType IS NULL OR r.roomType IN :roomType) AND " +
           "(:floor IS NULL OR r.floorNumber IN :floor) AND " +
           "(:amenities IS NULL OR EXISTS (SELECT 1 FROM r.features f WHERE f IN :amenities))")
    List<Room> findFilteredRooms(@Param("roomType") List<String> roomType,
                                 @Param("floor") List<Integer> floor,
                                 @Param("amenities") List<String> amenities);
    
    @Query("SELECT b FROM Booking b JOIN b.bookingRooms br JOIN br.room r " +
           "WHERE r.roomId = :roomId " +
           "AND ((b.checkInDate <= :endDate AND b.checkOutDate >= :startDate) " +
           "OR (b.checkInDate <= :endDate AND b.actualCheckIn IS NOT NULL AND b.actualCheckOut IS NULL)) " +
           "AND b.status NOT IN ('CANCELLED') " +
           "ORDER BY b.checkInDate")
    List<Booking> findBookingsForRoomAndDateRange(@Param("roomId") Long roomId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    // List view queries
    @Query("SELECT b FROM Booking b " +
           "JOIN b.guest g " +
           "JOIN b.bookingRooms br " +
           "JOIN br.room r " +
           "WHERE (:status IS NULL OR b.status = :status) " +
           "AND (:searchQuery IS NULL OR " +
           "LOWER(g.firstName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "LOWER(g.lastName) LIKE LOWER(CONCAT('%', :searchQuery, '%')) OR " +
           "b.bookingCode LIKE CONCAT('%', :searchQuery, '%') OR " +
           "r.roomNumber LIKE CONCAT('%', :searchQuery, '%')) " +
           "AND (:roomType IS NULL OR r.roomType IN :roomType) " +
           "AND (:floor IS NULL OR r.floorNumber IN :floor) " +
           "AND (:source IS NULL OR b.bookingSource IN :source) " +
           "AND (:paymentStatus IS NULL OR b.paymentStatus IN :paymentStatus) " +
           "AND (:bookingStatus IS NULL OR b.status IN :bookingStatus)")
    Page<Booking> findFilteredReservations(@Param("searchQuery") String searchQuery,
                                           @Param("status") String status,
                                           @Param("roomType") List<String> roomType,
                                           @Param("floor") List<Integer> floor,
                                           @Param("source") List<String> source,
                                           @Param("paymentStatus") List<String> paymentStatus,
                                           @Param("bookingStatus") List<String> bookingStatus,
                                           Pageable pageable);
    
    // Month view queries
    @Query("SELECT FUNCTION('DATE', b.checkInDate) as date, " +
           "COUNT(DISTINCT br.room.roomId) as occupiedRooms, " +
           "SUM(br.roomRate) as revenue, " +
           "COUNT(DISTINCT CASE WHEN b.checkInDate = FUNCTION('DATE', b.checkInDate) THEN b.bookingId END) as arrivals, " +
           "COUNT(DISTINCT CASE WHEN b.checkOutDate = FUNCTION('DATE', b.checkOutDate) THEN b.bookingId END) as departures " +
           "FROM Booking b " +
           "JOIN b.bookingRooms br " +
           "WHERE YEAR(b.checkInDate) = :year AND MONTH(b.checkInDate) = :month " +
           "GROUP BY FUNCTION('DATE', b.checkInDate)")
    List<Object[]> findMonthlyOccupancyData(@Param("year") int year,
                                           @Param("month") int month);
    
    // Dashboard stats queries
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.checkInDate = :today " +
           "AND b.status NOT IN ('CHECKED_IN', 'CHECKED_OUT', 'CANCELLED')")
    Long countTodaysArrivals(@Param("today") LocalDate today);
    
    @Query("SELECT COUNT(b) FROM Booking b " +
           "WHERE b.checkOutDate = :today " +
           "AND b.status IN ('CHECKED_IN')")
    Long countTodaysDepartures(@Param("today") LocalDate today);
    
    @Query("SELECT " +
           "(COUNT(DISTINCT br.room.roomId) * 100.0 / (SELECT COUNT(*) FROM Room)) as occupancyRate, " +
           "COALESCE(SUM(br.roomRate), 0) as revenue " +
           "FROM Booking b " +
           "JOIN b.bookingRooms br " +
           "WHERE b.checkInDate <= :today AND b.checkOutDate >= :today " +
           "AND b.status IN ('CHECKED_IN', 'CONFIRMED')")
    Map<String, Object> getTodaysOccupancyAndRevenue(@Param("today") LocalDate today);
    
    // Booking status counts
    @Query("SELECT b.status, COUNT(b) FROM Booking b " +
           "WHERE b.checkInDate <= :today AND b.checkOutDate >= :today " +
           "GROUP BY b.status")
    List<Object[]> getBookingStatusCounts(@Param("today") LocalDate today);
    
    // Total rooms count
    @Query("SELECT COUNT(r) FROM Room r")
    Long getTotalRooms();
}