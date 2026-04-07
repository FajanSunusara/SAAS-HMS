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
import java.util.Map;
import java.util.Optional;
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByGuestGuestId(Long guestId);

    List<Booking> findByStatus(BookingStatus status);

    Page<Booking> findByStatus(BookingStatus status, Pageable pageable);

    List<Booking> findByCheckInDate(LocalDate checkInDate);

    List<Booking> findByCheckOutDate(LocalDate checkOutDate);

    // ---------- TODAY ----------
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :date AND b.status = :status")
    List<Booking> findTodayCheckIns(@Param("date") LocalDate date,
                                    @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :date AND b.status = :status")
    List<Booking> findTodayCheckOuts(@Param("date") LocalDate date,
                                     @Param("status") BookingStatus status);

    // ---------- DATE RANGE ----------
    @Query("SELECT b FROM Booking b WHERE b.checkInDate BETWEEN :start AND :end")
    List<Booking> findBookingsBetweenDates(@Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :date AND b.status <> :status")
    List<Booking> findByCheckInDateAndStatusNot(@Param("date") LocalDate date,
                                                @Param("status") BookingStatus status);

    // ---------- OCCUPIED ----------
    @Query("""
        SELECT b FROM Booking b
        WHERE :date BETWEEN b.checkInDate AND b.checkOutDate
        AND b.status = :status
    """)
    List<Booking> findOccupiedRoomsByDate(@Param("date") LocalDate date,
                                          @Param("status") BookingStatus status);

    // ---------- COUNTS ----------
    Long countByStatus(BookingStatus status);
//    Long countByStatus(RoomStatus status);


    Long countByCheckInDateAndStatus(LocalDate date, BookingStatus status);

    Long countByCheckOutDateAndStatus(LocalDate date, BookingStatus status);

    Long countByCheckInDateAndStatusIn(LocalDate date, List<BookingStatus> statuses);

    // ---------- SEARCH ----------
    @Query("""
        SELECT b FROM Booking b
        WHERE LOWER(b.guest.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(b.guest.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR b.bookingCode LIKE CONCAT('%', :keyword, '%')
    """)
    Page<Booking> searchBookings(@Param("keyword") String keyword, Pageable pageable);

    // ---------- CURRENT STAY ----------
    @Query("""
        SELECT b FROM Booking b
        WHERE b.guest.guestId = :guestId
        AND :date BETWEEN b.checkInDate AND b.checkOutDate
        AND b.status = :status
    """)
    Optional<Booking> findCurrentStay(@Param("guestId") Long guestId,
                                      @Param("date") LocalDate date,
                                      @Param("status") BookingStatus status);

    // ---------- GUEST HISTORY ----------
    Optional<Booking> findFirstByGuestGuestIdAndStatus(Long guestId, BookingStatus status);

    Long countByGuestGuestIdAndStatus(Long guestId, BookingStatus status);

    Page<Booking> findByGuestGuestId(Long guestId, Pageable pageable);

    List<Booking> findByGuestGuestIdAndStatusOrderByCheckInDateDesc(Long guestId,
                                                                    BookingStatus status);

    List<Booking> findTop10ByGuestGuestIdOrderByCheckInDateDesc(Long guestId);

    // ---------- CALENDAR ----------
    @Query("""
        SELECT b FROM Booking b
        WHERE :startDate < b.checkOutDate
        AND :endDate > b.checkInDate
        AND b.status <> :cancelledStatus
    """)
    List<Booking> findBookingsInDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate,
                                          @Param("cancelledStatus") BookingStatus cancelledStatus);

    // ---------- OCCUPIED ROOMS COUNT ----------
    @Query("""
        SELECT COUNT(br)
        FROM BookingRoom br
        JOIN br.booking b
        WHERE :date BETWEEN b.checkInDate AND b.checkOutDate
        AND b.status IN (:statuses)
    """)
    Long countOccupiedRoomsOnDate(@Param("date") LocalDate date,
                                  @Param("statuses") List<BookingStatus> statuses);
    
    @Query("""
    	    SELECT b.status AS status, COUNT(b) AS count
    	    FROM Booking b
    	    WHERE :date BETWEEN b.checkInDate AND b.checkOutDate
    	    GROUP BY b.status
    	""")
    	List<Map<String, Object>> countBookingsByStatusForDate(
    	        @Param("date") LocalDate date
    	);
    
    
    @Query("""
    	    SELECT DISTINCT b FROM Booking b
    	    JOIN b.bookingRooms br
    	    WHERE b.checkInDate = :date
    	    AND b.status IN :statuses
    	""")
    	List<Booking> findTodayCheckInsDashboardPage(
    	        @Param("date") LocalDate date,
    	        @Param("statuses") List<BookingStatus> statuses
    	);

    @Query("""
    	    SELECT DISTINCT b FROM Booking b
    	    JOIN b.bookingRooms br
    	    WHERE b.checkOutDate = :date
    	    AND b.status IN :statuses
    	""")
    	List<Booking> findTodayCheckOutsDashboardPage(
    	        @Param("date") LocalDate date,
    	        @Param("statuses") List<BookingStatus> statuses
    	);

}
