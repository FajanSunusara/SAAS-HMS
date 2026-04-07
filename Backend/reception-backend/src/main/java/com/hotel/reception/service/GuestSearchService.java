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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)  // Changed to read-only
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
        List<Booking> currentBookings = bookingRepository.findOccupiedRoomsByDate(today, BookingStatus.CHECKED_IN);
        
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
            try {
                BookingStatus targetStatus = BookingStatus.valueOf(status.toUpperCase());
                currentBookings = currentBookings.stream()
                    .filter(booking -> booking.getStatus().equals(targetStatus))
                    .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
            }
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
        long currentGuests = bookingRepository.findOccupiedRoomsByDate(today, BookingStatus.CHECKED_IN).size();
        
        // Get today's check-ins
        long checkInsToday = bookingRepository.countByCheckInDateAndStatus(today, BookingStatus.CHECKED_IN);
        
        // Get expected check-ins (confirmed bookings for today)
        long expectedCheckIns = bookingRepository.countByCheckInDateAndStatus(today, BookingStatus.CONFIRMED);
        
        // Get today's check-outs
        long checkOutsToday = bookingRepository.countByCheckOutDateAndStatus(today, BookingStatus.CHECKED_IN);
        
        // Calculate occupancy rate
        long totalRooms = 180L; // This should come from RoomRepository
        long occupiedRooms = bookingRepository.findOccupiedRoomsByDate(today, BookingStatus.CHECKED_IN).size();
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
    
    // Removed @Transactional annotation - read-only from class level applies
    public GuestSummaryStats getGuestSummaryStats() {
        log.info("Fetching guest summary stats");
        
        try {
            List<Guest> allGuests = guestRepository.findAll();
            
            if (allGuests.isEmpty()) {
                return createEmptyStats();
            }
            
            // Find most frequent guest and highest LTV guest in a single pass for efficiency
            Guest mostFrequentGuest = null;
            int maxStays = 0;
            Guest highestLtvGuest = null;
            BigDecimal maxLtv = BigDecimal.ZERO;
            
            // Pre-calculate stays for all guests to avoid multiple queries
            Map<Long, Integer> guestStaysMap = allGuests.stream()
                .collect(Collectors.toMap(
                    Guest::getGuestId,
                    guest -> bookingRepository.findByGuestGuestId(guest.getGuestId()).size()
                ));
            
            // Pre-calculate LTV for all guests
            Map<Long, BigDecimal> guestLtvMap = allGuests.stream()
                .collect(Collectors.toMap(
                    Guest::getGuestId,
                    this::calculateLifetimeValueSafe
                ));
            
            // Find guests with max values
            for (Guest guest : allGuests) {
                Long guestId = guest.getGuestId();
                int stays = guestStaysMap.getOrDefault(guestId, 0);
                BigDecimal ltv = guestLtvMap.getOrDefault(guestId, BigDecimal.ZERO);
                
                if (stays > maxStays) {
                    maxStays = stays;
                    mostFrequentGuest = guest;
                }
                
                if (ltv.compareTo(maxLtv) > 0) {
                    maxLtv = ltv;
                    highestLtvGuest = guest;
                }
            }
            
            return buildGuestSummaryStats(allGuests.size(), mostFrequentGuest, maxStays, 
                                          highestLtvGuest, maxLtv, guestStaysMap);
                
        } catch (Exception e) {
            log.error("Error in getGuestSummaryStats: {}", e.getMessage(), e);
            return createEmptyStats();
        }
    }
    
    private GuestSummaryStats createEmptyStats() {
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
    
    private GuestSummaryStats buildGuestSummaryStats(int totalGuestsCount, Guest mostFrequentGuest, 
                                                    int maxStays, Guest highestLtvGuest, 
                                                    BigDecimal maxLtv, Map<Long, Integer> guestStaysMap) {
        return GuestSummaryStats.builder()
            .mostFrequentGuest(mostFrequentGuest != null ? 
                mostFrequentGuest.getFirstName() + " " + mostFrequentGuest.getLastName() : "N/A")
            .mostFrequentGuestStays(maxStays)
            .mostFrequentGuestValue(mostFrequentGuest != null ? 
                calculateLifetimeValueSafe(mostFrequentGuest) : BigDecimal.ZERO)
            .highestLtvGuest(highestLtvGuest != null ? 
                highestLtvGuest.getFirstName() + " " + highestLtvGuest.getLastName() : "N/A")
            .highestLtvGuestStays(highestLtvGuest != null ? 
                guestStaysMap.getOrDefault(highestLtvGuest.getGuestId(), 0) : 0)
            .highestLtvGuestValue(maxLtv)
            .totalGuests((long) totalGuestsCount)
            .build();
    }
    
    private CurrentGuestResponse mapToCurrentGuestResponse(Booking booking) {
        // Get room information
        String roomNumber = "N/A";
        String roomType = "N/A";
        if (!booking.getBookingRooms().isEmpty()) {
            roomNumber = booking.getBookingRooms().get(0).getRoom().getRoomNumber();
            roomType = booking.getBookingRooms().get(0).getRoom().getRoomType();
        }
        
        // Calculate balance - handle potential exception
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = bookingService.getBookingBill(booking.getBookingId()).getBalanceDue();
        } catch (Exception e) {
            log.warn("Error getting balance for booking {}: {}", booking.getBookingId(), e.getMessage());
        }
        
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
            .status(booking.getStatus().name())
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
        BigDecimal lifetimeValue = calculateLifetimeValueSafe(guest);
        
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
                b.getStatus().equals(BookingStatus.CHECKED_IN)
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
    
    /**
     * Safe version of calculateLifetimeValue that won't throw exceptions
     */
    private BigDecimal calculateLifetimeValueSafe(Guest guest) {
        try {
            List<Booking> guestBookings = bookingRepository.findByGuestGuestId(guest.getGuestId());
            
            if (guestBookings.isEmpty()) {
                return BigDecimal.ZERO;
            }
            
            BigDecimal total = BigDecimal.ZERO;
            for (Booking booking : guestBookings) {
                try {
                    // Try to get amount from booking first
                    if (bookingService.getBookingBill(booking.getBookingId()).getTotalAmount() != null) {
                        total = total.add(bookingService.getBookingBill(booking.getBookingId()).getTotalAmount());
                    } else {
                        // Fall back to service call
                        BigDecimal amount = bookingService.getBookingBill(booking.getBookingId()).getTotalAmount();
                        if (amount != null) {
                            total = total.add(amount);
                        }
                    }
                } catch (Exception e) {
                    log.debug("Error getting amount for booking {}: {}", booking.getBookingId(), e.getMessage());
                    // Continue with next booking
                }
            }
            return total;
            
        } catch (Exception e) {
            log.warn("Error calculating LTV for guest {}: {}", guest.getGuestId(), e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Original method kept for compatibility - delegates to safe version
     */
    private BigDecimal calculateLifetimeValue(Guest guest) {
        return calculateLifetimeValueSafe(guest);
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