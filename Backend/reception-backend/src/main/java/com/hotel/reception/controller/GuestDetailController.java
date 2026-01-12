package com.hotel.reception.controller;

import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.GuestDetailResponse;
import com.hotel.reception.service.GuestDetailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/guest-detail")
@RequiredArgsConstructor
@Tag(name = "Guest Detail Management", description = "APIs for comprehensive guest detail information")
public class GuestDetailController {

    private final GuestDetailService guestDetailService;

    @Operation(summary = "Get comprehensive guest details by ID")
    @GetMapping("/{guestId}")
    public ResponseEntity<ApiResponse<GuestDetailResponse>> getGuestDetails(@PathVariable Long guestId) {
        GuestDetailResponse response = guestDetailService.getGuestDetails(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest details by guest code")
    @GetMapping("/code/{guestCode}")
    public ResponseEntity<ApiResponse<GuestDetailResponse>> getGuestDetailsByCode(@PathVariable String guestCode) {
        GuestDetailResponse response = guestDetailService.getGuestDetailsByCode(guestCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's current stay details")
    @GetMapping("/{guestId}/current-stay")
    public ResponseEntity<ApiResponse<GuestDetailResponse.CurrentStayResponse>> getGuestCurrentStay(
            @PathVariable Long guestId) {
        GuestDetailResponse.CurrentStayResponse response = guestDetailService.getGuestCurrentStay(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's booking history")
    @GetMapping("/{guestId}/booking-history")
    public ResponseEntity<ApiResponse<GuestDetailResponse.BookingHistorySection>> getGuestBookingHistory(
            @PathVariable Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        GuestDetailResponse.BookingHistorySection response = 
            guestDetailService.getGuestBookingHistory(guestId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's payment history")
    @GetMapping("/{guestId}/payment-history")
    public ResponseEntity<ApiResponse<GuestDetailResponse.PaymentHistorySection>> getGuestPaymentHistory(
            @PathVariable Long guestId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        GuestDetailResponse.PaymentHistorySection response = 
            guestDetailService.getGuestPaymentHistory(guestId, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's service history")
    @GetMapping("/{guestId}/service-history")
    public ResponseEntity<ApiResponse<GuestDetailResponse.ServiceHistorySection>> getGuestServiceHistory(
            @PathVariable Long guestId) {
        GuestDetailResponse.ServiceHistorySection response = guestDetailService.getGuestServiceHistory(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's preferences")
    @GetMapping("/{guestId}/preferences")
    public ResponseEntity<ApiResponse<GuestDetailResponse.PreferencesSection>> getGuestPreferences(
            @PathVariable Long guestId) {
        GuestDetailResponse.PreferencesSection response = guestDetailService.getGuestPreferences(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's activity timeline")
    @GetMapping("/{guestId}/activity-timeline")
    public ResponseEntity<ApiResponse<GuestDetailResponse.ActivityTimelineSection>> getGuestActivityTimeline(
            @PathVariable Long guestId) {
        GuestDetailResponse.ActivityTimelineSection response = guestDetailService.getGuestActivityTimeline(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get guest's financial summary")
    @GetMapping("/{guestId}/financial-summary")
    public ResponseEntity<ApiResponse<GuestDetailResponse.FinancialSummaryResponse>> getGuestFinancialSummary(
            @PathVariable Long guestId) {
        GuestDetailResponse.FinancialSummaryResponse response = guestDetailService.getGuestFinancialSummary(guestId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}