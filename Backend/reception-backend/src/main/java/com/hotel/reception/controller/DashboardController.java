package com.hotel.reception.controller;

import com.hotel.reception.repository.BookingRepository;
import com.hotel.reception.repository.GuestRepository;
import com.hotel.reception.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @Autowired
    private GuestRepository guestRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats() {
        try {
            // Guest Statistics
            long totalGuests = guestRepository.count();
            long vipGuests = guestRepository.findByVipStatus("VIP").size();
            
            // Room Statistics
            long totalRooms = roomRepository.count();
            long availableRooms = roomRepository.countByStatus("AVAILABLE");
            long occupiedRooms = roomRepository.countByStatus("OCCUPIED");
            double occupancyRate = totalRooms > 0 ? 
                (double) occupiedRooms / totalRooms * 100 : 0;
            
            // Booking Statistics
            long totalBookings = bookingRepository.count();
            long todayCheckIns = bookingRepository.countCheckInsToday(LocalDate.now());
            long todayCheckOuts = bookingRepository.countCheckOutsToday(LocalDate.now());
            long activeBookings = bookingRepository.countByStatus("CHECKED_IN");
            
            // Revenue Statistics (mock - you'll need to implement actual revenue calculation)
            double estimatedRevenue = activeBookings * 5000; // Mock calculation
            double monthlyRevenue = estimatedRevenue * 30;
            double yearlyRevenue = monthlyRevenue * 12;
            
            Map<String, Object> stats = new HashMap<>();
            
            // Summary Cards
            stats.put("totalGuests", totalGuests);
            stats.put("vipGuests", vipGuests);
            stats.put("totalRooms", totalRooms);
            stats.put("availableRooms", availableRooms);
            stats.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
            stats.put("activeBookings", activeBookings);
            stats.put("todayCheckIns", todayCheckIns);
            stats.put("todayCheckOuts", todayCheckOuts);
            
            // Revenue
            stats.put("estimatedRevenue", estimatedRevenue);
            stats.put("monthlyRevenue", monthlyRevenue);
            stats.put("yearlyRevenue", yearlyRevenue);
            
            // Room Type Distribution (mock - you'll need to implement this)
            Map<String, Integer> roomTypeDistribution = new HashMap<>();
            roomTypeDistribution.put("Standard", 20);
            roomTypeDistribution.put("Deluxe", 10);
            roomTypeDistribution.put("Suite", 5);
            roomTypeDistribution.put("VIP Suite", 3);
            stats.put("roomTypeDistribution", roomTypeDistribution);
            
            // Booking Source Distribution (mock)
            Map<String, Integer> bookingSourceDistribution = new HashMap<>();
            bookingSourceDistribution.put("Walk-in", 45);
            bookingSourceDistribution.put("Website", 30);
            bookingSourceDistribution.put("Travel Agency", 15);
            bookingSourceDistribution.put("Corporate", 10);
            stats.put("bookingSourceDistribution", bookingSourceDistribution);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get dashboard statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}