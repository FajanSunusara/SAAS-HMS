package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GuestRepository extends JpaRepository<Guest, Long> {
    
    Optional<Guest> findByEmail(String email);
    
    Optional<Guest> findByPhone(String phone);
    
    Optional<Guest> findByLoyaltyNumber(String loyaltyNumber);
    
    Optional<Guest> findByIdNumberAndIdType(String idNumber, String idType);
    
    List<Guest> findByVipStatus(String vipStatus);
    
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(CONCAT(g.firstName, ' ', g.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "g.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(g.company) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Guest> searchGuests(@Param("keyword") String keyword);
    
    @Query("SELECT g FROM Guest g WHERE " +
           "LOWER(CONCAT(g.firstName, ' ', g.lastName)) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "g.phone LIKE CONCAT('%', :keyword, '%')")
    Page<Guest> searchGuestsWithPagination(@Param("keyword") String keyword, Pageable pageable);
    
    Long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT COUNT(DISTINCT b.guest.guestId) FROM Booking b WHERE b.createdAt BETWEEN :start AND :end")
    Long countUniqueGuestsInPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
