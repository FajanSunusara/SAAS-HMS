package com.hotel.reception.service;

import com.hotel.reception.model.dto.response.CurrentGuestResponse;
import com.hotel.reception.model.dto.response.GuestHistoryResponse;
import com.hotel.reception.model.dto.response.GuestStatsResponse;
import com.hotel.reception.model.dto.response.GuestSummaryStats;
import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.repository.BookingRepository;
import com.hotel.reception.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GuestSearchService {
    
    private final GuestRepository guestRepository;
    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    private final BookingService bookingService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    public Page<CurrentGuestResponse> searchCurrentGuests(String keyword, String status, Pageable pageable) {
        LocalDate today = LocalDate.now();
        
        // Get current bookings
        List<Booking> currentBookings = bookingRepository.findOccupiedRoomsByDate(today);
        
        // Filter by keyword if provided
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchTerm = keyword.toLowerCase().trim();
            currentBookings = currentBookings.stream()
                .filter(booking -> 
                    booking.getGuest().getFirstName().toLowerCase().contains(searchTerm) ||
                    booking.getGuest().getLastName().toLowerCase().contains(searchTerm) ||
                    booking.getGuest().getEmail().toLowerCase().contains(searchTerm) ||
                    booking.getGuest().getPhone().contains(searchTerm) ||
                    booking.getBookingCode().toLowerCase().contains(searchTerm) ||
                    booking.getBookingRooms().stream()
                        .anyMatch(br -> br.getRoom().getRoomNumber().contains(searchTerm))
                )
                .collect(Collectors.toList());
        }
        
        // Filter by status if provided
        if (status != null && !status.equalsIgnoreCase("all")) {
            currentBookings = currentBookings.stream()
                .filter(booking -> booking.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        }
        
        // Convert to CurrentGuestResponse
        List<CurrentGuestResponse> responses = currentBookings.stream()
            .map(this::mapToCurrentGuestResponse)
            .collect(Collectors.toList());
        
        // For simplicity, we'll return all results without pagination in this example
        // In production, you'd want to implement proper pagination in the repository
        return new org.springframework.data.domain.PageImpl<>(
            responses, 
            pageable, 
            responses.size()
        );
    }
    
    public Page<GuestHistoryResponse> searchAllGuestsWithHistory(String keyword, String status, Pageable pageable) {
        // Get all guests with pagination
        Page<Guest> guestPage;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            guestPage = guestRepository.searchGuestsWithPagination(keyword, pageable);
        } else {
            guestPage = guestRepository.findAll(pageable);
        }
        
        return guestPage.map(this::mapToGuestHistoryResponse);
    }
    
    public GuestStatsResponse getDashboardStats() {
        LocalDate today = LocalDate.now();
        
        // Get current guests count
        long currentGuests = bookingRepository.findOccupiedRoomsByDate(today).size();
        
        // Get today's check-ins
        long checkInsToday = bookingRepository.countByCheckInDateAndStatus(today, BookingStatus.CHECKED_IN.name());
        
        // Get expected check-ins (confirmed bookings for today)
        long expectedCheckIns = bookingRepository.countByCheckInDateAndStatus(today, BookingStatus.CONFIRMED.name());
        
        // Get today's check-outs
        long checkOutsToday = bookingRepository.countByCheckOutDateAndStatus(today, BookingStatus.CHECKED_IN.name());
        
        // Calculate occupancy rate
        long totalRooms = 180L; // This should come from RoomRepository
        long occupiedRooms = bookingRepository.findOccupiedRoomsByDate(today).size();
        double occupancyRate = totalRooms > 0 ? 
            (double) occupiedRooms / totalRooms * 100 : 0;
        
        return GuestStatsResponse.builder()
            .currentGuests(currentGuests)
            .checkInsToday(checkInsToday)
            .expectedCheckIns(expectedCheckIns)
            .checkOutsToday(checkOutsToday)
            .occupancyRate(Math.round(occupancyRate * 10.0) / 10.0)
            .totalRooms(totalRooms)
            .occupiedRooms(occupiedRooms)
            .build();
    }
    
    @Transactional(noRollbackFor = {RuntimeException.class, Exception.class})
    public GuestSummaryStats getGuestSummaryStats() {
        log.info("Fetching guest summary stats");
        
        try {
            List<Guest> allGuests = guestRepository.findAll();
            
            if (allGuests.isEmpty()) {
                return GuestSummaryStats.builder()
                    .mostFrequentGuest("N/A")
                    .mostFrequentGuestStays(0)
                    .mostFrequentGuestValue(BigDecimal.ZERO)
                    .highestLtvGuest("N/A")
                    .highestLtvGuestStays(0)
                    .highestLtvGuestValue(BigDecimal.ZERO)
                    .totalGuests(0L)
                    .build();
            }
            
            // Find most frequent guest
            Guest mostFrequentGuest = null;
            int maxStays = 0;
            for (Guest guest : allGuests) {
                int stays = bookingRepository.findByGuestGuestId(guest.getGuestId()).size();
                if (stays > maxStays) {
                    maxStays = stays;
                    mostFrequentGuest = guest;
                }
            }
            
            // Find highest lifetime value guest
            Guest highestLtvGuest = null;
            BigDecimal maxLtv = BigDecimal.ZERO;
            for (Guest guest : allGuests) {
                BigDecimal ltv = calculateLifetimeValue(guest);
                if (ltv.compareTo(maxLtv) > 0) {
                    maxLtv = ltv;
                    highestLtvGuest = guest;
                }
            }
            
            return GuestSummaryStats.builder()
                .mostFrequentGuest(mostFrequentGuest != null ? 
                    mostFrequentGuest.getFirstName() + " " + mostFrequentGuest.getLastName() : "N/A")
                .mostFrequentGuestStays(mostFrequentGuest != null ? maxStays : 0)
                .mostFrequentGuestValue(mostFrequentGuest != null ? 
                    calculateLifetimeValue(mostFrequentGuest) : BigDecimal.ZERO)
                .highestLtvGuest(highestLtvGuest != null ? 
                    highestLtvGuest.getFirstName() + " " + highestLtvGuest.getLastName() : "N/A")
                .highestLtvGuestStays(highestLtvGuest != null ? 
                    bookingRepository.findByGuestGuestId(highestLtvGuest.getGuestId()).size() : 0)
                .highestLtvGuestValue(highestLtvGuest != null ? maxLtv : BigDecimal.ZERO)
                .totalGuests((long) allGuests.size())
                .build();
                
        } catch (Exception e) {
            log.error("Error in getGuestSummaryStats: {}", e.getMessage(), e);
            // Return safe default instead of throwing
            return GuestSummaryStats.builder()
                .mostFrequentGuest("N/A")
                .mostFrequentGuestStays(0)
                .mostFrequentGuestValue(BigDecimal.ZERO)
                .highestLtvGuest("N/A")
                .highestLtvGuestStays(0)
                .highestLtvGuestValue(BigDecimal.ZERO)
                .totalGuests(0L)
                .build();
        }
    }
    
    private CurrentGuestResponse mapToCurrentGuestResponse(Booking booking) {
        // Get room information
        String roomNumber = "N/A";
        String roomType = "N/A";
        if (!booking.getBookingRooms().isEmpty()) {
            roomNumber = booking.getBookingRooms().get(0).getRoom().getRoomNumber();
            roomType = booking.getBookingRooms().get(0).getRoom().getRoomType();
        }
        
        // Calculate balance
        BigDecimal balance = bookingService.getBookingBill(booking.getBookingId()).getBalanceDue();
        
        // Calculate nights
        long nights = booking.getCheckInDate().until(booking.getCheckOutDate()).getDays();
        
        return CurrentGuestResponse.builder()
            .guestId(booking.getGuest().getGuestId())
            .guestCode("GUEST-" + booking.getGuest().getGuestId())
            .name(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName())
            .email(booking.getGuest().getEmail())
            .phone(booking.getGuest().getPhone())
            .roomNumber(roomNumber)
            .roomType(roomType)
            .checkInDate(booking.getCheckInDate())
            .checkOutDate(booking.getCheckOutDate())
            .nights((int) nights)
            .balance(balance)
            .status(booking.getStatus())
            .vipLevel(booking.getGuest().getVipStatus())
            .loyaltyTier(mapLoyaltyNumberToTier(booking.getGuest().getLoyaltyNumber()))
            .build();
    }
    
    private GuestHistoryResponse mapToGuestHistoryResponse(Guest guest) {
        // Get guest's bookings
        List<Booking> guestBookings = bookingRepository.findByGuestGuestId(guest.getGuestId());
        
        // Calculate total stays
        int totalStays = guestBookings.size();
        
        // Calculate lifetime value
        BigDecimal lifetimeValue = calculateLifetimeValue(guest);
        
        // Get last stay information
        Booking lastStay = guestBookings.stream()
            .filter(b -> b.getCheckOutDate() != null)
            .max((b1, b2) -> b2.getCheckOutDate().compareTo(b1.getCheckOutDate()))
            .orElse(null);
        
        String lastStayFormatted = null;
        String lastRoom = null;
        if (lastStay != null) {
            lastStayFormatted = lastStay.getCheckInDate().format(DATE_FORMATTER) + 
                              " - " + lastStay.getCheckOutDate().format(DATE_FORMATTER);
            
            if (!lastStay.getBookingRooms().isEmpty()) {
                lastRoom = lastStay.getBookingRooms().get(0).getRoom().getRoomNumber();
            }
        }
        
        // Get current status
        String currentStatus = "inactive";
        LocalDate today = LocalDate.now();
        boolean hasCurrentBooking = guestBookings.stream()
            .anyMatch(b -> 
                b.getCheckInDate().isBefore(today.plusDays(1)) && 
                b.getCheckOutDate().isAfter(today) &&
                b.getStatus().equals(BookingStatus.CHECKED_IN.name())
            );
        
        if (hasCurrentBooking) {
            currentStatus = "checked-in";
        }
        
        return GuestHistoryResponse.builder()
            .guestId(guest.getGuestId())
            .guestCode("GUEST-" + guest.getGuestId())
            .name(guest.getFirstName() + " " + guest.getLastName())
            .email(guest.getEmail())
            .phone(guest.getPhone())
            .createdAt(guest.getCreatedAt())
            .vipLevel(guest.getVipStatus())
            .loyaltyTier(mapLoyaltyNumberToTier(guest.getLoyaltyNumber()))
            .lastStay(lastStayFormatted)
            .lastRoom(lastRoom)
            .totalStays(totalStays)
            .lifetimeValue(lifetimeValue)
            .status(currentStatus)
            .build();
    }
    
//    private BigDecimal calculateLifetimeValue(Guest guest) {
//        List<Booking> guestBookings = bookingRepository.findByGuestGuestId(guest.getGuestId());
//        
//        return guestBookings.stream()
//            .map(booking -> {
//                try {
//                    return bookingService.getBookingBill(booking.getBookingId()).getTotalAmount();
//                } catch (Exception e) {
//                    return BigDecimal.ZERO;
//                }
//            })
//            .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//    
    
    private BigDecimal calculateLifetimeValue(Guest guest) {
        try {
            List<Booking> guestBookings = bookingRepository.findByGuestGuestId(guest.getGuestId());
            
            if (guestBookings.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            BigDecimal total = BigDecimal.ZERO;
            for (Booking booking : guestBookings) {
                if (bookingService.getBookingBill(booking.getBookingId()).getTotalAmount() != null) {
                    total = total.add(bookingService.getBookingBill(booking.getBookingId()).getTotalAmount());
                }
            }
            return total;
            
        } catch (Exception e) {
            log.warn("Error calculating LTV for guest {}: {}", guest.getGuestId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    private String mapLoyaltyNumberToTier(String loyaltyNumber) {
        if (loyaltyNumber == null || loyaltyNumber.isEmpty()) {
            return "Basic";
        }
        
        // Simple mapping based on loyalty number prefix or length
        // In production, you'd have a proper loyalty tier system
        if (loyaltyNumber.startsWith("D")) return "Diamond";
        if (loyaltyNumber.startsWith("P")) return "Platinum";
        if (loyaltyNumber.startsWith("G")) return "Gold";
        if (loyaltyNumber.startsWith("S")) return "Silver";
        return "Basic";
    }
}