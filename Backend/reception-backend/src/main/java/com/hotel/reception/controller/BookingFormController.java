package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.BookingFormRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.BookingFormResponse;
import com.hotel.reception.service.BookingFormService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/booking-form")
@RequiredArgsConstructor
@Tag(name = "Booking Form", description = "APIs for standard booking form")
public class BookingFormController {
    
    private final BookingFormService bookingFormService;
    
    @Operation(summary = "Create new booking from form")
    @PostMapping
    public ResponseEntity<ApiResponse<BookingFormResponse>> createBooking(
            @Valid @RequestBody BookingFormRequest request) {
        BookingFormResponse response = bookingFormService.createBooking(request);
        return new ResponseEntity<>(
                ApiResponse.success("Booking created successfully", response),
                HttpStatus.CREATED
        );
    }
    @Operation(summary = "Get booking by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingFormResponse>> getBookingById(@PathVariable Long id) {
        BookingFormResponse response = bookingFormService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get booking by code")
    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<BookingFormResponse>> getBookingByCode(@PathVariable String code) {
        BookingFormResponse response = bookingFormService.getBookingByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}