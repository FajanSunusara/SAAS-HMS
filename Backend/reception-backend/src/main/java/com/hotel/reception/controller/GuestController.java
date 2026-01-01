package com.hotel.reception.controller;

import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/guests")
public class GuestController {
    
    @Autowired
    private GuestRepository guestRepository;
    
    // GET all guests
    @GetMapping
    public ResponseEntity<List<Guest>> getAllGuests() {
        List<Guest> guests = guestRepository.findAll();
        return ResponseEntity.ok(guests);
    }
    
    // GET guest by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGuestById(@PathVariable Long id) {
        Optional<Guest> guest = guestRepository.findById(id);
        if (guest.isPresent()) {
            return ResponseEntity.ok(guest.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Guest not found with ID: " + id);
            return ResponseEntity.status(404).body(error);
        }
    }
    
    // POST create new guest
    @PostMapping
    public ResponseEntity<?> createGuest(@RequestBody Guest guestRequest) {
        try {
            // Check if email already exists
            if (guestRequest.getEmail() != null) {
                Optional<Guest> existingGuest = guestRepository.findByEmail(guestRequest.getEmail());
                if (existingGuest.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Guest with email already exists: " + guestRequest.getEmail());
                    return ResponseEntity.status(400).body(error);
                }
            }
            
            // Check if phone already exists
            if (guestRequest.getPhone() != null) {
                Optional<Guest> existingGuest = guestRepository.findByPhone(guestRequest.getPhone());
                if (existingGuest.isPresent()) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Guest with phone already exists: " + guestRequest.getPhone());
                    return ResponseEntity.status(400).body(error);
                }
            }
            
            // Set timestamps
            guestRequest.setCreatedAt(LocalDateTime.now());
            guestRequest.setUpdatedAt(LocalDateTime.now());
            
            // Save to database
            Guest savedGuest = guestRepository.save(guestRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Guest created successfully");
            response.put("guestId", savedGuest.getGuestId());
            response.put("guest", savedGuest);
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create guest: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // PUT update guest
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGuest(@PathVariable Long id, @RequestBody Guest guestUpdates) {
        try {
            Optional<Guest> existingGuest = guestRepository.findById(id);
            if (existingGuest.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Guest not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            Guest guest = existingGuest.get();
            
            // Update fields if provided
            if (guestUpdates.getFirstName() != null) guest.setFirstName(guestUpdates.getFirstName());
            if (guestUpdates.getLastName() != null) guest.setLastName(guestUpdates.getLastName());
            if (guestUpdates.getEmail() != null) guest.setEmail(guestUpdates.getEmail());
            if (guestUpdates.getPhone() != null) guest.setPhone(guestUpdates.getPhone());
            if (guestUpdates.getCountryCode() != null) guest.setCountryCode(guestUpdates.getCountryCode());
            if (guestUpdates.getNationality() != null) guest.setNationality(guestUpdates.getNationality());
            if (guestUpdates.getDateOfBirth() != null) guest.setDateOfBirth(guestUpdates.getDateOfBirth());
            if (guestUpdates.getAddress() != null) guest.setAddress(guestUpdates.getAddress());
            if (guestUpdates.getCity() != null) guest.setCity(guestUpdates.getCity());
            if (guestUpdates.getState() != null) guest.setState(guestUpdates.getState());
            if (guestUpdates.getCountry() != null) guest.setCountry(guestUpdates.getCountry());
            if (guestUpdates.getZipCode() != null) guest.setZipCode(guestUpdates.getZipCode());
            if (guestUpdates.getIdType() != null) guest.setIdType(guestUpdates.getIdType());
            if (guestUpdates.getIdNumber() != null) guest.setIdNumber(guestUpdates.getIdNumber());
            if (guestUpdates.getPassportNumber() != null) guest.setPassportNumber(guestUpdates.getPassportNumber());
            if (guestUpdates.getPassportExpiry() != null) guest.setPassportExpiry(guestUpdates.getPassportExpiry());
            if (guestUpdates.getLoyaltyNumber() != null) guest.setLoyaltyNumber(guestUpdates.getLoyaltyNumber());
            if (guestUpdates.getVipStatus() != null) guest.setVipStatus(guestUpdates.getVipStatus());
            
            guest.setUpdatedAt(LocalDateTime.now());
            
            Guest updatedGuest = guestRepository.save(guest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Guest updated successfully");
            response.put("guest", updatedGuest);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update guest: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // DELETE guest
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGuest(@PathVariable Long id) {
        try {
            Optional<Guest> guest = guestRepository.findById(id);
            if (guest.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Guest not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            guestRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Guest deleted successfully");
            response.put("guestId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete guest: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // SEARCH guests
    @GetMapping("/search")
    public ResponseEntity<?> searchGuests(@RequestParam String keyword) {
        try {
            List<Guest> guests = guestRepository.searchGuests(keyword);
            return ResponseEntity.ok(guests);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Search failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // GET guest statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getGuestStatistics() {
        try {
            long totalGuests = guestRepository.count();
            List<Guest> vipGuests = guestRepository.findByVipStatus("VIP");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalGuests", totalGuests);
            stats.put("vipGuests", vipGuests.size());
            stats.put("regularGuests", totalGuests - vipGuests.size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    
}