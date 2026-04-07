package com.hotel.reception.service;

import com.hotel.reception.mapper.BookingMapper;
import com.hotel.reception.model.dto.response.*;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final RoomService roomService;
    private final PaymentService paymentService;
    
    public DashboardResponse getDashboardStats() {
        log.debug("Generating dashboard statistics");
        
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        
        // Today's check-ins and check-outs
        Long todayCheckIns = bookingRepository.countByCheckInDateAndStatus(today, BookingStatus.CONFIRMED);
        Long todayCheckOuts = bookingRepository.countByCheckOutDateAndStatus(today, BookingStatus.CHECKED_IN);
        
        // Current occupancy
        Long currentOccupancy = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
        
        // Room status counts
        Long totalRooms = roomService.getTotalRoomCount();
        Long availableRooms = roomService.getCountByStatus("AVAILABLE");
        Long occupiedRooms = roomService.getCountByStatus("OCCUPIED");
        Long reservedRooms = roomService.getCountByStatus("RESERVED");
        Long maintenanceRooms = roomService.getCountByStatus("MAINTENANCE");
        Long cleaningRooms = roomService.getCountByStatus("CLEANING");
        
        // Occupancy rate
        Double occupancyRate = totalRooms > 0 ? 
                (occupiedRooms.doubleValue() / totalRooms.doubleValue()) * 100 : 0.0;
        
        // Today's revenue
        BigDecimal todayRevenue = paymentService.getTodayCollection();
        
        // Pending payments
        BigDecimal pendingPayments = invoiceRepository.getTotalPendingAmount();
        
        // Monthly revenue
        LocalDate startOfMonth = today.withDayOfMonth(1);
        BigDecimal monthlyRevenue = invoiceRepository.getTotalRevenueByDateRange(startOfMonth, today);
        
        // Weekly revenue
        LocalDate startOfWeek = today.minusDays(7);
        BigDecimal weeklyRevenue = invoiceRepository.getTotalRevenueByDateRange(startOfWeek, today);
        
        // Total revenue
        BigDecimal totalRevenue = invoiceRepository.getTotalRevenueByDateRange(
                LocalDate.now().minusYears(1), today);
        
        // Booking counts
        Long totalBookings = bookingRepository.count();
        Long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        Long checkedInBookings = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
        Long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);
        
        // Payment method breakdown
        Map<String, BigDecimal> paymentMethodBreakdown = paymentService.getPaymentMethodBreakdown(
                today.minusDays(30), today);
        
        // Upcoming check-ins/outs
        Long upcomingCheckIns = bookingRepository.countByCheckInDateAndStatus(
                today.plusDays(1), BookingStatus.CONFIRMED);
        Long upcomingCheckOuts = bookingRepository.countByCheckOutDateAndStatus(
                today.plusDays(1), BookingStatus.CHECKED_IN);
        
        // Calculate KPIs
        BigDecimal averageDailyRate = calculateAverageDailyRate();
        BigDecimal revenuePerAvailableRoom = calculateRevPAR(todayRevenue, totalRooms);
        
        return DashboardResponse.builder()
                .date(today)
                .todayCheckIns(todayCheckIns.intValue())
                .todayCheckOuts(todayCheckOuts.intValue())
                .currentOccupancy(currentOccupancy.intValue())
                .todayRevenue(todayRevenue)
                .pendingPayments(pendingPayments != null ? pendingPayments : BigDecimal.ZERO)
                .totalRooms(totalRooms.intValue())
                .availableRooms(availableRooms.intValue())
                .occupiedRooms(occupiedRooms.intValue())
                .reservedRooms(reservedRooms.intValue())
                .maintenanceRooms(maintenanceRooms.intValue())
                .cleaningRooms(cleaningRooms.intValue())
                .occupancyRate(occupancyRate)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .monthlyRevenue(monthlyRevenue != null ? monthlyRevenue : BigDecimal.ZERO)
                .weeklyRevenue(weeklyRevenue != null ? weeklyRevenue : BigDecimal.ZERO)
                .totalBookings(totalBookings.intValue())
                .confirmedBookings(confirmedBookings.intValue())
                .checkedInBookings(checkedInBookings.intValue())
                .cancelledBookings(cancelledBookings.intValue())
                .totalCollection(todayRevenue)
                .paymentMethodBreakdown(paymentMethodBreakdown)
                .upcomingCheckIns(upcomingCheckIns.intValue())
                .upcomingCheckOuts(upcomingCheckOuts.intValue())
                .averageDailyRate(averageDailyRate)
                .revenuePerAvailableRoom(revenuePerAvailableRoom)
                .build();
    }
    
    private BigDecimal calculateAverageDailyRate() {
        // Simplified ADR calculation
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        
        BigDecimal monthlyRevenue = invoiceRepository.getTotalRevenueByDateRange(startOfMonth, today);
        Long occupiedRoomNights = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);
        
        if (monthlyRevenue != null && occupiedRoomNights > 0) {
            return monthlyRevenue.divide(BigDecimal.valueOf(occupiedRoomNights), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
    private BigDecimal calculateRevPAR(BigDecimal revenue, Long totalRooms) {
        if (revenue != null && totalRooms > 0) {
            return revenue.divide(BigDecimal.valueOf(totalRooms), 2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
    
//    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;

    public List<DashboardBookingResponse> getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        List<BookingStatus> arrivalStatuses = List.of(
                BookingStatus.CONFIRMED,
                BookingStatus.CHECKED_IN
        );
        return bookingRepository
                .findTodayCheckInsDashboardPage(today, arrivalStatuses)
                .stream()
                .map(bookingMapper::toDashboardResponse)
                .collect(Collectors.toList());
    }

    
    public List<DashboardBookingResponse> getTodayCheckOuts() {
        LocalDate today = LocalDate.now();

        List<BookingStatus> departureStatuses = List.of(
                BookingStatus.CHECKED_IN
        );

        return bookingRepository.findTodayCheckOutsDashboardPage(today, departureStatuses)
        		.stream()
                .map(bookingMapper::toDashboardResponse)
                .collect(Collectors.toList());
    }
    
    

}
