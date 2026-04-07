package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.BookingRequest;
import com.hotel.reception.model.dto.request.CheckoutRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.BookingBillResponse;
import com.hotel.reception.model.dto.response.BookingResponse;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking Management", description = "APIs for managing bookings (Walk-in, Group, Company)")

public class BookingController {
    
    private final BookingService bookingService;
    
    @Operation(summary = "Create new booking", description = "Supports SINGLE, GROUP, and COMPANY bookings")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(
                ApiResponse.success("Booking created successfully", response),
                HttpStatus.CREATED
        );
    }
    
    @Operation(summary = "Get booking by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get booking by booking code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByCode(@PathVariable String code) {
        BookingResponse response = bookingService.getBookingByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get all bookings")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> responses = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Search bookings")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> searchBookings(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<BookingResponse> responses = bookingService.searchBookings(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get today's check-ins")
    @GetMapping("/checkins/today")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayCheckIns() {
        List<BookingResponse> responses = bookingService.getTodayCheckIns();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get today's check-outs")
    @GetMapping("/checkouts/today")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayCheckOuts() {
        List<BookingResponse> responses = bookingService.getTodayCheckOuts();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Update booking status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        try {
            BookingStatus bookingStatus = BookingStatus.valueOf(status.toUpperCase());
            BookingResponse response = bookingService.updateBookingStatus(id, bookingStatus);
            return ResponseEntity.ok(ApiResponse.success("Booking status updated", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid booking status: " + status)
            );
        }
    }
    
    @Operation(summary = "Cancel booking")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(
            @PathVariable Long id,
            @RequestParam(required = false) String reason) {
        
        bookingService.cancelBooking(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", null));
    }
    
    
    @Operation(summary = "Process check-in for booking")
    @PatchMapping("/{id}/checkin")
    public ResponseEntity<ApiResponse<BookingResponse>> processCheckIn(
            @PathVariable Long id,
            @RequestBody Map<String, Object> checkinData) {
        
        BookingResponse response = bookingService.processCheckIn(id, checkinData);
        return ResponseEntity.ok(ApiResponse.success("Check-in processed successfully", response));
    }
    
    @Operation(summary = "Process check-out for booking")
    @PostMapping("/{id}/checkout")
    public ResponseEntity<ApiResponse<BookingResponse>> processCheckOut(
            @PathVariable Long id,
            @RequestBody CheckoutRequest checkoutRequest) {
        
        BookingResponse response = bookingService.processCheckOut(id, checkoutRequest);
        return ResponseEntity.ok(ApiResponse.success("Check-out processed successfully", response));
    }

    @Operation(summary = "Get today's expected departures")
    @GetMapping("/departures/today")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayDepartures() {
        List<BookingResponse> responses = bookingService.getTodayDepartures();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Get booking bill summary")
    @GetMapping("/{id}/bill")
    public ResponseEntity<ApiResponse<BookingBillResponse>> getBookingBill(@PathVariable Long id) {
        BookingBillResponse response = bookingService.getBookingBill(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Search departures")
    @GetMapping("/search/departures")
    public ResponseEntity<ApiResponse<Page<BookingResponse>>> searchDepartures(
            @RequestParam String keyword,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<BookingResponse> responses = bookingService.searchDepartures(keyword, roomType, status, paymentMethod, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    

    @Operation(summary = "Cancel check-in")
    @PatchMapping("/{id}/checkin/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelCheckIn(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String reason = request.get("reason");
        BookingResponse response = bookingService.cancelCheckIn(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Check-in cancelled", response));
    }

    @Operation(summary = "Get today's expected arrivals")
    @GetMapping("/arrivals/today")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getTodayArrivals() {
        List<BookingResponse> responses = bookingService.getTodayArrivals();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
