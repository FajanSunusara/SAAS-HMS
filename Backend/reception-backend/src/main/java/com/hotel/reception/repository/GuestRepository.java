package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    
    Optional<Guest> findByEmail(String email);
    
    Optional<Guest> findByPhone(String phone);
    
    Optional<Guest> findByLoyaltyNumber(String loyaltyNumber);
    
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(g.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "g.email LIKE CONCAT('%', :keyword, '%') OR " +
           "g.phone LIKE CONCAT('%', :keyword, '%')")
    List<Guest> searchGuests(@Param("keyword") String keyword);
    
    List<Guest> findByCity(String city);
    
    List<Guest> findByNationality(String nationality);
    
    List<Guest> findByVipStatus(String vipStatus);
    
    Long countByCreatedAtBetween(java.time.LocalDateTime start, java.time.LocalDateTime end);
}