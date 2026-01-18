package com.hotel.reception.controller;

import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.ReservationDashboardResponse;
import com.hotel.reception.model.dto.response.ReservationListResponse;
import com.hotel.reception.model.dto.response.CalendarViewResponse;
import com.hotel.reception.model.dto.response.MonthOverviewResponse;
import com.hotel.reception.model.dto.response.FilterOptionsResponse;
import com.hotel.reception.model.dto.response.BookingStatusCountResponse;
import com.hotel.reception.service.ReservationDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/v1/reservation-dashboard")
@RequiredArgsConstructor
@Tag(name = "Reservation Dashboard", description = "APIs for reservation dashboard and calendar views")
public class ReservationDashboardController {

    private final ReservationDashboardService reservationDashboardService;

//    @Operation(summary = "Get dashboard statistics")
//    @GetMapping("/stats")
//    public ResponseEntity<ApiResponse<ReservationDashboardResponse>> getDashboardStats(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        if (date == null) {
//            date = LocalDate.now();
//        }
//        ReservationDashboardResponse response = reservationDashboardService.getDashboardStats(date);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    @Operation(summary = "Get calendar view data for date range")
//    @GetMapping("/calendar")
//    public ResponseEntity<ApiResponse<CalendarViewResponse>> getCalendarView(
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate centerDate,
//            @RequestParam(required = false, defaultValue = "3") int daysBefore,
//            @RequestParam(required = false, defaultValue = "3") int daysAfter) {
//        
//        CalendarViewResponse response = reservationDashboardService.getCalendarView(centerDate, daysBefore, daysAfter);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

    @Operation(summary = "Get list view data with filters")
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<Page<ReservationListResponse>>> getReservationList(
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String floor,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) List<String> amenities,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortDirection,
            Pageable pageable) {
        
        Page<ReservationListResponse> response = reservationDashboardService.getReservationList(
                searchQuery, status, roomType, floor, source, paymentStatus, 
                amenities, sortBy, sortDirection, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

//    @Operation(summary = "Get month overview data")
//    @GetMapping("/month-overview")
//    public ResponseEntity<ApiResponse<MonthOverviewResponse>> getMonthOverview(
//            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") String monthYear,
//            @RequestParam(required = false) String roomType) {
//        
//        MonthOverviewResponse response = reservationDashboardService.getMonthOverview(monthYear, roomType);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//
//    @Operation(summary = "Get booking status counts")
//    @GetMapping("/status-counts")
//    public ResponseEntity<ApiResponse<List<BookingStatusCountResponse>>> getBookingStatusCounts(
//            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
//        
//        if (date == null) {
//            date = LocalDate.now();
//        }
//        List<BookingStatusCountResponse> response = reservationDashboardService.getBookingStatusCounts(date);
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }

//    @Operation(summary = "Get filter options")
//    @GetMapping("/filter-options")
//    public ResponseEntity<ApiResponse<FilterOptionsResponse>> getFilterOptions() {
//        FilterOptionsResponse response = reservationDashboardService.getFilterOptions();
//        return ResponseEntity.ok(ApiResponse.success(response));
//    }
//    
    
    

    @Operation(summary = "Get dashboard statistics")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<ReservationDashboardResponse>> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        ReservationDashboardResponse response = reservationDashboardService.getDashboardStats(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get calendar view data for date range")
    @GetMapping("/calendar")
    public ResponseEntity<ApiResponse<CalendarViewResponse>> getCalendarView(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate centerDate,
            @RequestParam(required = false, defaultValue = "3") int daysBefore,
            @RequestParam(required = false, defaultValue = "3") int daysAfter) {
        
        if (centerDate == null) {
            centerDate = LocalDate.now();
        }
        
        CalendarViewResponse response = reservationDashboardService.getCalendarView(centerDate, daysBefore, daysAfter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get month overview data")
    @GetMapping("/month-overview")
    public ResponseEntity<ApiResponse<MonthOverviewResponse>> getMonthOverview(
            @RequestParam(required = false) String monthYear,
            @RequestParam(required = false) String roomType) {
        
        if (monthYear == null) {
            monthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        }
        
        MonthOverviewResponse response = reservationDashboardService.getMonthOverview(monthYear, roomType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get booking status counts")
    @GetMapping("/status-counts")
    public ResponseEntity<ApiResponse<List<BookingStatusCountResponse>>> getBookingStatusCounts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        if (date == null) {
            date = LocalDate.now();
        }
        List<BookingStatusCountResponse> response = reservationDashboardService.getBookingStatusCounts(date);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get filter options")
    @GetMapping("/filter-options")
    public ResponseEntity<ApiResponse<FilterOptionsResponse>> getFilterOptions() {
        FilterOptionsResponse response = reservationDashboardService.getFilterOptions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    
}

// DTOs for responses


