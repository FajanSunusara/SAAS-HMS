package com.hotel.reception.repository;

import com.hotel.reception.model.entity.BookingRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRoomRepository extends JpaRepository<BookingRoom, Long> {
    
    List<BookingRoom> findByBookingBookingId(Long bookingId);
    
    List<BookingRoom> findByRoomRoomId(Long roomId);
    
    @Query("SELECT br FROM BookingRoom br WHERE br.booking.bookingId = :bookingId AND br.guest.guestId = :guestId")
    List<BookingRoom> findByBookingAndGuest(
            @Param("bookingId") Long bookingId,
            @Param("guestId") Long guestId
    );
}
