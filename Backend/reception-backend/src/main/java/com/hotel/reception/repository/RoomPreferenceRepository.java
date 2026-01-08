package com.hotel.reception.repository;

import com.hotel.reception.model.entity.RoomPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomPreferenceRepository extends JpaRepository<RoomPreference, Long> {
    
    Optional<RoomPreference> findByBookingBookingId(Long bookingId);
    
    // Add this method to get list (though there should only be one per booking)
//    List<RoomPreference> findByBookingBookingId(Long bookingId);
}