package com.hotel.reception.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    
    // Today's summary
    private LocalDate date;
    private Integer todayCheckIns;
    private Integer todayCheckOuts;
    private Integer currentOccupancy;
    private BigDecimal todayRevenue;
    private BigDecimal pendingPayments;
    
    // Room status
    private Integer totalRooms;
    private Integer availableRooms;
    private Integer occupiedRooms;
    private Integer reservedRooms;
    private Integer maintenanceRooms;
    private Integer cleaningRooms;
    
    // Occupancy rate
    private Double occupancyRate;
    private Double occupancyRateLastWeek;
    private Double occupancyRateLastMonth;
    
    // Revenue
    private BigDecimal totalRevenue;
    private BigDecimal monthlyRevenue;
    private BigDecimal weeklyRevenue;
    
    // Bookings
    private Integer totalBookings;
    private Integer confirmedBookings;
    private Integer checkedInBookings;
    private Integer cancelledBookings;
    
    // Payment summary
    private BigDecimal totalCollection;
    private Map<String, BigDecimal> paymentMethodBreakdown;
    
    // Upcoming
    private Integer upcomingCheckIns;
    private Integer upcomingCheckOuts;
    
    // KPIs
    private BigDecimal averageDailyRate;
    private BigDecimal revenuePerAvailableRoom;
}
