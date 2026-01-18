package com.hotel.reception.service;

import com.hotel.reception.model.dto.response.*;
import com.hotel.reception.model.entity.*;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.model.enums.RoomStatus;
import com.hotel.reception.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReservationDashboardServiceImpl implements ReservationDashboardService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final PaymentRepository paymentRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final BookingRoomRepository bookingRoomRepository;

//    @Override
//    public ReservationDashboardResponse getDashboardStats(LocalDate date) {
//        log.info("Getting dashboard stats for date: {}", date);
//        
//        // Today's arrivals
//        Long todayArrivals = bookingRepository.countByCheckInDateAndStatusIn(
//                date, Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.TENTATIVE));
//        
//        // Today's departures
//        Long todayDepartures = bookingRepository.countByCheckOutDateAndStatus(
//                date, BookingStatus.CHECKED_IN);
//        
//        // Occupancy rate
//        Long totalRooms = roomRepository.count();
//        Long occupiedRooms = roomRepository.countByStatus(RoomStatus.OCCUPIED);
//        BigDecimal occupancyRate = totalRooms > 0 ?
//                BigDecimal.valueOf(occupiedRooms * 100.0 / totalRooms).setScale(2) :
//                BigDecimal.ZERO;
//        
//        // Revenue today
//        BigDecimal revenueToday = paymentRepository.sumAmountPaidByPaymentDate(date)
//                .orElse(BigDecimal.ZERO);
//        
//        return ReservationDashboardResponse.builder()
//                .todayArrivals(todayArrivals.intValue())
//                .todayDepartures(todayDepartures.intValue())
//                .occupancyRate(occupancyRate)
//                .revenueToday(revenueToday)
//                .build();
//    }
//
//    @Override
//    public CalendarViewResponse getCalendarView(LocalDate centerDate, int daysBefore, int daysAfter) {
//        log.info("Getting calendar view for center date: {}", centerDate);
//        
//        LocalDate startDate = centerDate.minusDays(daysBefore);
//        LocalDate endDate = centerDate.plusDays(daysAfter + 1); // +1 to include end date
//        
//        // Get all rooms
//        List<Room> rooms = roomRepository.findAll();
//        List<RoomResponse> roomResponses = rooms.stream()
//                .map(this::mapToRoomResponse)
//                .collect(Collectors.toList());
//        
//        // Get bookings for the date range
//        List<Booking> bookings = bookingRepository.findBookingsInDateRange(startDate, endDate);
//        List<CalendarBookingResponse> bookingResponses = bookings.stream()
//                .map(this::mapToCalendarBookingResponse)
//                .collect(Collectors.toList());
//        
//        // Generate date range
//        List<LocalDate> dateRange = startDate.datesUntil(endDate)
//                .collect(Collectors.toList());
//        
//        return CalendarViewResponse.builder()
//                .rooms(roomResponses)
//                .bookings(bookingResponses) // Now this is correct type
//                .dateRange(dateRange)
//                .centerDate(centerDate)
//                .build();
//    }


    @Override
    public Page<ReservationListResponse> getReservationList(
            String searchQuery, String status, String roomType, String floor,
            String source, String paymentStatus, List<String> amenities,
            String sortBy, String sortDirection, Pageable pageable) {
        
        Specification<Booking> spec = Specification.where(null);
        
        // Add search query filter
        if (searchQuery != null && !searchQuery.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                String searchPattern = "%" + searchQuery.toLowerCase() + "%";
                return cb.or(
                    cb.like(cb.lower(root.get("bookingCode")), searchPattern),
                    cb.like(cb.lower(root.join("guest").get("firstName")), searchPattern),
                    cb.like(cb.lower(root.join("guest").get("lastName")), searchPattern)
                );
            });
        }
        
        // Add status filter - FIXED: Handle null/empty status
        if (status != null && !status.isEmpty() && !status.equals("all")) {
            try {
                // Convert hyphenated status to underscore for enum conversion
                String statusStr = status.replace('-', '_').toUpperCase();
                BookingStatus bookingStatus = BookingStatus.valueOf(statusStr);
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), bookingStatus));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid booking status provided: {}", status);
                // Don't filter by status if invalid value is provided
            }
        }
        
        // Add other filters
        if (roomType != null && !roomType.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.join("bookingRooms").join("room").get("roomType"), roomType));
        }
        
        if (floor != null && !floor.isEmpty()) {
            try {
                int floorNum = Integer.parseInt(floor);
                spec = spec.and((root, query, cb) -> 
                    cb.equal(root.join("bookingRooms").join("room").get("floorNumber"), floorNum));
            } catch (NumberFormatException e) {
                log.warn("Invalid floor number: {}", floor);
            }
        }
        
        // Execute query with pagination
        Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
        
        return bookings.map(this::mapToReservationListResponse);
    }

//    @Override
//    public MonthOverviewResponse getMonthOverview(String monthYear, String roomType) {
//        YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("yyyy-MM"));
//        LocalDate startDate = yearMonth.atDay(1);
//        LocalDate endDate = yearMonth.atEndOfMonth();
//        
//        // Get daily statistics
//        List<DailyStatisticResponse> dailyStats = new ArrayList<>();
//        
//        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
//            // Calculate occupancy for the day
//            Long occupiedRooms = bookingRepository.countOccupiedRoomsOnDate(date);
//            Long totalRooms = roomRepository.count();
//            BigDecimal occupancy = totalRooms > 0 ?
//                    BigDecimal.valueOf(occupiedRooms * 100.0 / totalRooms) :
//                    BigDecimal.ZERO;
//            
//            // Calculate revenue for the day
//            BigDecimal revenue = paymentRepository.sumAmountPaidByPaymentDate(date)
//                    .orElse(BigDecimal.ZERO);
//            
//            // Count arrivals and departures
//            Long arrivals = bookingRepository.countByCheckInDateAndStatus(date, BookingStatus.CHECKED_IN);
//            Long departures = bookingRepository.countByCheckOutDateAndStatus(date, BookingStatus.CHECKED_OUT);
//            
//            dailyStats.add(DailyStatisticResponse.builder()
//                    .date(date)
//                    .occupancy(occupancy)
//                    .revenue(revenue)
//                    .arrivals(arrivals.intValue())
//                    .departures(departures.intValue())
//                    .build());
//        }
//        
//        // Calculate month statistics
//        BigDecimal totalRevenue = dailyStats.stream()
//                .map(DailyStatisticResponse::getRevenue)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        
//        BigDecimal avgOccupancy = dailyStats.stream()
//                .map(DailyStatisticResponse::getOccupancy)
//                .reduce(BigDecimal.ZERO, BigDecimal::add)
//                .divide(BigDecimal.valueOf(dailyStats.size()), 2);
//        
//        BigDecimal peakOccupancy = dailyStats.stream()
//                .map(DailyStatisticResponse::getOccupancy)
//                .max(BigDecimal::compareTo)
//                .orElse(BigDecimal.ZERO);
//        
//        Integer totalArrivals = dailyStats.stream()
//                .mapToInt(DailyStatisticResponse::getArrivals)
//                .sum();
//        
//        Integer totalDepartures = dailyStats.stream()
//                .mapToInt(DailyStatisticResponse::getDepartures)
//                .sum();
//        
//        MonthStatisticsResponse monthStats = MonthStatisticsResponse.builder()
//                .totalRevenue(totalRevenue)
//                .avgOccupancy(avgOccupancy)
//                .peakOccupancy(peakOccupancy)
//                .totalArrivals(totalArrivals)
//                .totalDepartures(totalDepartures)
//                .build();
//        
//        return MonthOverviewResponse.builder()
//                .dailyStatistics(dailyStats)
//                .monthStatistics(monthStats)
//                .monthYear(monthYear)
//                .build();
//    }

//    @Override
//    public List<BookingStatusCountResponse> getBookingStatusCounts(LocalDate date) {
//        Map<BookingStatus, Long> counts = bookingRepository.countBookingsByStatusForDate(date);
//        
//        Map<BookingStatus, String> colorMap = new HashMap<>();
//        colorMap.put(BookingStatus.CONFIRMED, "bg-blue-500");
//        colorMap.put(BookingStatus.CHECKED_IN, "bg-green-500");
//        colorMap.put(BookingStatus.TENTATIVE, "bg-yellow-500");
//        colorMap.put(BookingStatus.NO_SHOW, "bg-red-500");
//        colorMap.put(BookingStatus.CANCELLED, "bg-gray-500");
//        
//        return counts.entrySet().stream()
//                .map(entry -> BookingStatusCountResponse.builder()
//                        .label(entry.getKey().name().replace("_", " "))
//                        .count(entry.getValue())
//                        .color(colorMap.getOrDefault(entry.getKey(), "bg-gray-500"))
//                        .value(entry.getKey().name().toLowerCase().replace("_", "-"))
//                        .build())
//                .collect(Collectors.toList());
//    }

    @Override
    public FilterOptionsResponse getFilterOptions() {
        List<String> roomTypes = roomRepository.findDistinctRoomTypes();
        List<String> floors = roomRepository.findDistinctFloors().stream()
                .map(String::valueOf)
                .collect(Collectors.toList());
        
        // Hard-coded amenities (could be from database)
        List<String> amenities = Arrays.asList("WiFi", "TV", "AC", "Mini Bar", "Balcony", "Sea View", "Jacuzzi");
        
        // Hard-coded sources and payment statuses
        List<String> sources = Arrays.asList("Website", "OTA", "Phone", "Travel Agent", "Walk-in");
        List<String> paymentStatuses = Arrays.asList("Paid", "Pending", "Partially Paid", "Refunded");
        
        return FilterOptionsResponse.builder()
                .roomTypes(roomTypes)
                .floors(floors)
                .amenities(amenities)
                .sources(sources)
                .paymentStatuses(paymentStatuses)
                .build();
    }

    private RoomResponse mapToRoomResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .floorNumber(room.getFloorNumber())
                .baseRate(room.getBaseRate())
                .status(room.getStatus())
                .capacity(room.getCapacity())
                .features(room.getFeatures()) // Pass String[] directly
                .build();
    }
    private CalendarBookingResponse mapToCalendarBookingResponse(Booking booking) {
        // Get assigned room
        Room room = booking.getBookingRooms().isEmpty() ? null : 
                   booking.getBookingRooms().get(0).getRoom();
        
        return CalendarBookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .roomId(room != null ? room.getRoomId() : null)
                .roomNumber(room != null ? room.getRoomNumber() : null)
                .guestId(booking.getGuest().getGuestId())
                .guestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .status(formatStatus(booking.getStatus())) // Fixed: using helper method
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .source(booking.getBookingSource())
                .paymentStatus(booking.getPaymentStatus())
                .build();
    }

    private ReservationListResponse mapToReservationListResponse(Booking booking) {
        // Get assigned room
        Room room = booking.getBookingRooms().isEmpty() ? null : 
                   booking.getBookingRooms().get(0).getRoom();
        
        // Calculate nights and amount
        long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
        BigDecimal amount = room != null ? 
                room.getBaseRate().multiply(BigDecimal.valueOf(nights)) : BigDecimal.ZERO;
        
        return ReservationListResponse.builder()
                .id(booking.getBookingId())
                .bookingId(booking.getBookingCode())
                .guestId(booking.getGuest().getGuestId())
                .guestName(booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName())
                .roomNumber(room != null ? room.getRoomNumber() : null)
                .roomType(room != null ? room.getRoomType() : null)
                .status(formatStatus(booking.getStatus())) // Fixed: using helper method
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .nights((int) nights)
                .amount(amount)
                .paymentStatus(booking.getPaymentStatus())
                .source(booking.getBookingSource())
                .floor(room != null ? String.valueOf(room.getFloorNumber()) : null)
                .specialRequests(booking.getSpecialInstructions())
                .createdAt(booking.getCreatedAt())
                .build();
    }
    
    // Helper method to format status string
    private String formatStatus(String status) {
        if (status == null) return "confirmed";
        return status.toLowerCase().replace("_", "-");
    }

    // Helper method to format status label
    private String formatStatusLabel(String status) {
        if (status == null) return "Confirmed";
        return status.replace("_", " ");
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @Override
    public ReservationDashboardResponse getDashboardStats(LocalDate date) {
        log.info("Getting dashboard stats for date: {}", date);
        
        // Today's arrivals
Long todayArrivals = bookingRepository.countByCheckInDateAndStatus(date,"CONFIRMED");
        
        // Today's departures
        Long todayDepartures = bookingRepository.countByCheckOutDateAndStatus(date, "CHECKED_IN");
        
        // Occupancy rate - Use String "OCCUPIED" instead of RoomStatus enum
        Long totalRooms = roomRepository.count();
        Long occupiedRooms = roomRepository.countByStatus("OCCUPIED"); // Use String
        
        BigDecimal occupancyRate = totalRooms > 0 ?
                BigDecimal.valueOf(occupiedRooms * 100.0 / totalRooms).setScale(2, RoundingMode.HALF_UP) :
                BigDecimal.ZERO;
        
        // Revenue today
        BigDecimal revenueToday = paymentRepository.sumAmountPaidByPaymentDate(date)
                .orElse(BigDecimal.ZERO);
        
        return ReservationDashboardResponse.builder()
                .todayArrivals(todayArrivals.intValue())
                .todayDepartures(todayDepartures.intValue())
                .occupancyRate(occupancyRate)
                .revenueToday(revenueToday)
                .build();
    }

    @Override
    public CalendarViewResponse getCalendarView(LocalDate centerDate, int daysBefore, int daysAfter) {
        log.info("Getting calendar view for center date: {}", centerDate);
        
        LocalDate startDate = centerDate.minusDays(daysBefore);
        LocalDate endDate = centerDate.plusDays(daysAfter);
        
        // Get all rooms
        List<Room> rooms = roomRepository.findAll();
        List<RoomResponse> roomResponses = rooms.stream()
                .map(this::mapToRoomResponse)
                .collect(Collectors.toList());
        
        // Get bookings for the date range
        List<Booking> bookings = bookingRepository.findBookingsInDateRange(startDate, endDate);
        List<CalendarBookingResponse> bookingResponses = bookings.stream()
                .map(this::mapToCalendarBookingResponse)
                .collect(Collectors.toList());
        
        // Generate date range
        List<LocalDate> dateRange = startDate.datesUntil(endDate.plusDays(1))
                .collect(Collectors.toList());
        
        return CalendarViewResponse.builder()
                .rooms(roomResponses)
                .bookings(bookingResponses)
                .dateRange(dateRange)
                .centerDate(centerDate)
                .build();
    }

    @Override
    public MonthOverviewResponse getMonthOverview(String monthYear, String roomType) {
        // TRIM the monthYear to remove any whitespace/newline
        monthYear = monthYear.trim();
        YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("yyyy-MM"));
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        
        // Get daily statistics
        List<DailyStatisticResponse> dailyStats = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            // Calculate occupancy for the day
            Long occupiedRooms = bookingRepository.countOccupiedRoomsOnDate(date);
            Long totalRooms = roomRepository.count();
            BigDecimal occupancy = totalRooms > 0 ?
                    BigDecimal.valueOf(occupiedRooms * 100.0 / totalRooms) :
                    BigDecimal.ZERO;
            
            // Calculate revenue for the day
            BigDecimal revenue = paymentRepository.sumAmountPaidByPaymentDate(date)
                    .orElse(BigDecimal.ZERO);
            
            // Count arrivals and departures
            Long arrivals = bookingRepository.countByCheckInDateAndStatus(date, "CHECKED_IN");
            Long departures = bookingRepository.countByCheckOutDateAndStatus(date, "CHECKED_OUT");
            
            dailyStats.add(DailyStatisticResponse.builder()
                    .date(date)
                    .occupancy(occupancy.setScale(2, RoundingMode.HALF_UP))
                    .revenue(revenue)
                    .arrivals(arrivals.intValue())
                    .departures(departures.intValue())
                    .build());
        }
        
        // Calculate month statistics
        BigDecimal totalRevenue = dailyStats.stream()
                .map(DailyStatisticResponse::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgOccupancy = dailyStats.stream()
                .map(DailyStatisticResponse::getOccupancy)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(dailyStats.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal peakOccupancy = dailyStats.stream()
                .map(DailyStatisticResponse::getOccupancy)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        Integer totalArrivals = dailyStats.stream()
                .mapToInt(DailyStatisticResponse::getArrivals)
                .sum();
        
        Integer totalDepartures = dailyStats.stream()
                .mapToInt(DailyStatisticResponse::getDepartures)
                .sum();
        
        MonthStatisticsResponse monthStats = MonthStatisticsResponse.builder()
                .totalRevenue(totalRevenue)
                .avgOccupancy(avgOccupancy)
                .peakOccupancy(peakOccupancy)
                .totalArrivals(totalArrivals)
                .totalDepartures(totalDepartures)
                .build();
        
        return MonthOverviewResponse.builder()
                .dailyStatistics(dailyStats)
                .monthStatistics(monthStats)
                .monthYear(monthYear)
                .build();
    }

    @Override
    public List<BookingStatusCountResponse> getBookingStatusCounts(LocalDate date) {
        // FIXED: Repository now returns List<Map<String, Object>>
        List<Map<String, Object>> results = bookingRepository.countBookingsByStatusForDate(date);
        
        // Convert to Map<String, Long>
        Map<String, Long> countMap = results.stream()
                .collect(Collectors.toMap(
                    map -> (String) map.get("status"),
                    map -> (Long) map.get("count")
                ));
        
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("CONFIRMED", "bg-blue-500");
        colorMap.put("CHECKED_IN", "bg-green-500");
        colorMap.put("TENTATIVE", "bg-yellow-500");
        colorMap.put("NO_SHOW", "bg-red-500");
        colorMap.put("CANCELLED", "bg-gray-500");
        
        return countMap.entrySet().stream()
                .map(entry -> BookingStatusCountResponse.builder()
                        .label(entry.getKey().replace("_", " "))
                        .count(entry.getValue())
                        .color(colorMap.getOrDefault(entry.getKey(), "bg-gray-500"))
                        .value(entry.getKey().toLowerCase().replace("_", "-"))
                        .build())
                .collect(Collectors.toList());
    }
    
    
    
}