package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Room;
import com.hotel.reception.model.enums.RoomStatus;

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
    
//    Long countByStatus(String status);
    
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
    


Long countByStatus(RoomStatus status);
//    
//    @Query("SELECT DISTINCT r.roomType FROM Room r ORDER BY r.roomType")
//    List<String> findDistinctRoomTypes();
//    
//    @Query("SELECT DISTINCT r.floorNumber FROM Room r ORDER BY r.floorNumber")
//    List<Integer> findDistinctFloors();
//    
    // Find available rooms for date range
//    @Query("SELECT r FROM Room r WHERE r.roomId NOT IN (" +
//           "SELECT br.room.roomId FROM BookingRoom br " +
//           "JOIN br.booking b " +
//           "WHERE (:checkInDate < b.checkOutDate AND :checkOutDate > b.checkInDate) " +
//           "AND b.status NOT IN ('CANCELLED', 'CHECKED_OUT')) " +
//           "AND r.status = 'AVAILABLE' " +
//           "AND (:roomType IS NULL OR r.roomType = :roomType)")
//    
//    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkInDate,
//                                  @Param("checkOutDate") LocalDate checkOutDate,
//                                  @Param("roomType") String roomType);

//    Long countByStatus(String status);
//    
//    @Query("SELECT DISTINCT r.roomType FROM Room r")
//    List<String> findDistinctRoomTypes();
//    
//    @Query("SELECT DISTINCT r.floorNumber FROM Room r ORDER BY r.floorNumber")
//    List<Integer> findDistinctFloors();
//    
    Long countByStatus(String status);
    
    @Query("SELECT DISTINCT r.roomType FROM Room r")
    List<String> findDistinctRoomTypes();
    
    @Query("SELECT DISTINCT r.floorNumber FROM Room r ORDER BY r.floorNumber")
    List<Integer> findDistinctFloors();
    
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.roomType = :roomType AND " +
            "r.roomId NOT IN (" +
            "   SELECT br.room.roomId FROM BookingRoom br " +
            "   JOIN br.booking b " +
            "   WHERE b.status IN ('CONFIRMED', 'CHECKED_IN') AND " +
            "   b.checkInDate <= :checkOut AND " +
            "   b.checkOutDate >= :checkIn" +
            ")")
     List<Room> findAvailableRoomsByType(@Param("checkIn") LocalDate checkIn, 
                                         @Param("checkOut") LocalDate checkOut,
                                         @Param("roomType") String roomType);
    
}
