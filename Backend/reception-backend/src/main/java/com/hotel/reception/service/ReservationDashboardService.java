package com.hotel.reception.service;

import com.hotel.reception.model.dto.response.ReservationDashboardResponse;
import com.hotel.reception.model.dto.response.ReservationListResponse;
import com.hotel.reception.model.dto.response.BookingStatusCountResponse;
import com.hotel.reception.model.dto.response.CalendarViewResponse;
import com.hotel.reception.model.dto.response.FilterOptionsResponse;
import com.hotel.reception.model.dto.response.MonthOverviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ReservationDashboardService {
    
    ReservationDashboardResponse getDashboardStats(LocalDate date);
    
    CalendarViewResponse getCalendarView(LocalDate centerDate, int daysBefore, int daysAfter);
    
    Page<ReservationListResponse> getReservationList(
            String searchQuery, String status, String roomType, String floor, 
            String source, String paymentStatus, List<String> amenities,
            String sortBy, String sortDirection, Pageable pageable);
    
    MonthOverviewResponse getMonthOverview(String monthYear, String roomType);
    
    List<BookingStatusCountResponse> getBookingStatusCounts(LocalDate date);
    
    FilterOptionsResponse getFilterOptions();
}