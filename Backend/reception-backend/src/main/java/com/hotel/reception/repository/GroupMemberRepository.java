package com.hotel.reception.repository;

import com.hotel.reception.model.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    
    List<GroupMember> findByBookingBookingId(Long bookingId);
    
    Optional<GroupMember> findByBookingBookingIdAndIsLeaderTrue(Long bookingId);
    
    Long countByBookingBookingId(Long bookingId);
}
