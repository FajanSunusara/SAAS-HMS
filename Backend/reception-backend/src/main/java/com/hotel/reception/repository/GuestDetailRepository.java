package com.hotel.reception.repository;

import com.hotel.reception.model.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GuestDetailRepository extends JpaRepository<Guest, Long> {
    Optional<Guest> findByGuestId(Long guestId);
}