package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomNumber(String roomNumber);
    
    List<Room> findByStatus(String status);
    
    List<Room> findByRoomType(String roomType);
    
    List<Room> findByFloorNumber(Integer floorNumber);
    
    Long countByStatus(String status);
    
    @Query("SELECT r FROM Room r WHERE r.status = :status AND r.roomType = :type")
    List<Room> findByStatusAndType(@Param("status") String status, @Param("type") String type);
    
    @Query(value = """
        SELECT r.* FROM rooms r 
        WHERE r.status = 'AVAILABLE'
        AND r.room_id NOT IN (
            SELECT br.room_id FROM booking_rooms br
            JOIN bookings b ON br.booking_id = b.booking_id
            WHERE b.status IN ('CONFIRMED', 'CHECKED_IN')
            AND b.check_in_date <= :checkOut
            AND b.check_out_date >= :checkIn
        )
        AND (:roomType IS NULL OR r.room_type = :roomType)
        ORDER BY r.room_number
        """, nativeQuery = true)
    List<Room> findAvailableRooms(
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("roomType") String roomType
    );
    
    @Query("SELECT COUNT(r) FROM Room r")
    Long getTotalRoomCount();
}
