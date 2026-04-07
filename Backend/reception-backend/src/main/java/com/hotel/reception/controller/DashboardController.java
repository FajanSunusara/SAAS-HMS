package com.hotel.reception.controller;

import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.BookingResponse;
import com.hotel.reception.model.dto.response.DashboardBookingResponse;
import com.hotel.reception.model.dto.response.DashboardResponse;
import com.hotel.reception.service.DashboardService;
import com.hotel.reception.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and KPIs")
public class DashboardController {
    
    private final DashboardService dashboardService;
    private final BookingService bookingService;
    
    @Operation(summary = "Get complete dashboard statistics")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboardStats() {
        DashboardResponse response = dashboardService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    @Operation(summary = "Get today's check-ins")
    @GetMapping("/checkins/today")
    public ResponseEntity<ApiResponse<List<DashboardBookingResponse>>> getTodayCheckIns() {
        return ResponseEntity.ok(
            ApiResponse.success(dashboardService.getTodayCheckIns())
        );
    }

    @Operation(summary = "Get today's check-outs")
    @GetMapping("/checkouts/today")
    public ResponseEntity<ApiResponse<List<DashboardBookingResponse>>> getTodayCheckOuts() {
       
    	return ResponseEntity.ok(
                ApiResponse.success(dashboardService.getTodayCheckOuts())
            );
    }
    
    @Operation(summary = "Dashboard health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Dashboard API is running!");
    }
}
