package com.hotel.reception.controller;

import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.repository.BookingRepository;
import com.hotel.reception.repository.GuestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private GuestRepository guestRepository;
    
    // GET all bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }
    
    // GET booking by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        Optional<Booking> booking = bookingRepository.findById(id);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Booking not found with ID: " + id);
            return ResponseEntity.status(404).body(error);
        }
    }
    
    // GET booking by code
    @GetMapping("/code/{code}")
    public ResponseEntity<?> getBookingByCode(@PathVariable String code) {
        Optional<Booking> booking = bookingRepository.findByBookingCode(code);
        if (booking.isPresent()) {
            return ResponseEntity.ok(booking.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Booking not found with code: " + code);
            return ResponseEntity.status(404).body(error);
        }
    }
    
    // POST create new booking
    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Booking bookingRequest) {
        try {
            // Validate guest exists
            if (bookingRequest.getGuest() == null || bookingRequest.getGuest().getGuestId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Guest ID is required");
                return ResponseEntity.status(400).body(error);
            }
            
            Optional<Guest> guest = guestRepository.findById(bookingRequest.getGuest().getGuestId());
            if (guest.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Guest not found with ID: " + bookingRequest.getGuest().getGuestId());
                return ResponseEntity.status(404).body(error);
            }
            
            // Calculate nights
            if (bookingRequest.getCheckInDate() != null && bookingRequest.getCheckOutDate() != null) {
                long nights = ChronoUnit.DAYS.between(
                    bookingRequest.getCheckInDate(), 
                    bookingRequest.getCheckOutDate()
                );
                bookingRequest.setNights((int) nights);
            }
            
            // Generate unique booking code
            String bookingCode = "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            bookingRequest.setBookingCode(bookingCode);
            
            // Set default values
            if (bookingRequest.getStatus() == null) {
                bookingRequest.setStatus("CONFIRMED");
            }
            if (bookingRequest.getPaymentStatus() == null) {
                bookingRequest.setPaymentStatus("PENDING");
            }
            if (bookingRequest.getAdults() == null) {
                bookingRequest.setAdults(1);
            }
            if (bookingRequest.getChildren() == null) {
                bookingRequest.setChildren(0);
            }
            if (bookingRequest.getInfants() == null) {
                bookingRequest.setInfants(0);
            }
            if (bookingRequest.getTaxPercentage() == null) {
                bookingRequest.setTaxPercentage(BigDecimal.valueOf(10));
            }
            if (bookingRequest.getIncludeTax() == null) {
                bookingRequest.setIncludeTax(true);
            }
            
            // Set guest object
            bookingRequest.setGuest(guest.get());
            
            // Set timestamps
            bookingRequest.setCreatedAt(LocalDateTime.now());
            bookingRequest.setUpdatedAt(LocalDateTime.now());
            
            // Save to database
            Booking savedBooking = bookingRepository.save(bookingRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking created successfully");
            response.put("bookingId", savedBooking.getBookingId());
            response.put("bookingCode", savedBooking.getBookingCode());
            response.put("booking", savedBooking);
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create booking: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // PUT update booking
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBooking(@PathVariable Long id, @RequestBody Booking bookingUpdates) {
        try {
            Optional<Booking> existingBooking = bookingRepository.findById(id);
            if (existingBooking.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Booking not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            Booking booking = existingBooking.get();
            
            // Update fields if provided
            if (bookingUpdates.getCheckInDate() != null) booking.setCheckInDate(bookingUpdates.getCheckInDate());
            if (bookingUpdates.getCheckOutDate() != null) booking.setCheckOutDate(bookingUpdates.getCheckOutDate());
            if (bookingUpdates.getCheckInTime() != null) booking.setCheckInTime(bookingUpdates.getCheckInTime());
            if (bookingUpdates.getCheckOutTime() != null) booking.setCheckOutTime(bookingUpdates.getCheckOutTime());
            if (bookingUpdates.getStatus() != null) booking.setStatus(bookingUpdates.getStatus());
            if (bookingUpdates.getPaymentStatus() != null) booking.setPaymentStatus(bookingUpdates.getPaymentStatus());
            if (bookingUpdates.getAdults() != null) booking.setAdults(bookingUpdates.getAdults());
            if (bookingUpdates.getChildren() != null) booking.setChildren(bookingUpdates.getChildren());
            if (bookingUpdates.getInfants() != null) booking.setInfants(bookingUpdates.getInfants());
            if (bookingUpdates.getSpecialInstructions() != null) booking.setSpecialInstructions(bookingUpdates.getSpecialInstructions());
            if (bookingUpdates.getPurposeOfVisit() != null) booking.setPurposeOfVisit(bookingUpdates.getPurposeOfVisit());
            
            // Recalculate nights if dates changed
            if (bookingUpdates.getCheckInDate() != null || bookingUpdates.getCheckOutDate() != null) {
                long nights = ChronoUnit.DAYS.between(
                    booking.getCheckInDate(), 
                    booking.getCheckOutDate()
                );
                booking.setNights((int) nights);
            }
            
            booking.setUpdatedAt(LocalDateTime.now());
            
            Booking updatedBooking = bookingRepository.save(booking);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Booking updated successfully");
            response.put("booking", updatedBooking);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update booking: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // DELETE booking
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBooking(@PathVariable Long id) {
        try {
            Optional<Booking> booking = bookingRepository.findById(id);
            if (booking.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Booking not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            bookingRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Booking deleted successfully");
            response.put("bookingId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete booking: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // GET today's check-ins
//    @GetMapping("/today/checkins")
//    public ResponseEntity<?> getTodayCheckIns() {
//        try {
//            LocalDate today = LocalDate.now();
////            List<Booking> checkIns = bookingRepository.findByCheckInDate(today);
////            return ResponseEntity.ok(checkIns);
////        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("error", "Failed to get today's check-ins: " + e.getMessage());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
//    
    // GET today's check-outs
//    @GetMapping("/today/checkouts")
//    public ResponseEntity<?> getTodayCheckOuts() {
//        try {
//            LocalDate today = LocalDate.now();
//            List<Booking> checkOuts = bookingRepository.findByCheckOutDate(today);
//            return ResponseEntity.ok(checkOuts);
//        } catch (Exception e) {
//            Map<String, String> error = new HashMap<>();
//            error.put("error", "Failed to get today's check-outs: " + e.getMessage());
//            return ResponseEntity.status(500).body(error);
//        }
//    }
    
    // GET booking statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getBookingStatistics() {
        try {
            long totalBookings = bookingRepository.count();
            long confirmedBookings = bookingRepository.countByStatus("CONFIRMED");
            long checkedInBookings = bookingRepository.countByStatus("CHECKED_IN");
            long cancelledBookings = bookingRepository.countByStatus("CANCELLED");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalBookings", totalBookings);
            stats.put("confirmedBookings", confirmedBookings);
            stats.put("checkedInBookings", checkedInBookings);
            stats.put("cancelledBookings", cancelledBookings);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get booking statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}