package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.PaymentRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.PaymentResponse;
import com.hotel.reception.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "APIs for processing payments")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    @Operation(summary = "Process payment", description = "Accept payment via CASH, CARD, UPI, BANK_TRANSFER, etc.")
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.processPayment(request);
        return new ResponseEntity<>(
                ApiResponse.success("Payment processed successfully", response),
                HttpStatus.CREATED
        );
    }
    
    @Operation(summary = "Get payment by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(@PathVariable Long id) {
        PaymentResponse response = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get payments by invoice")
    @GetMapping("/invoice/{invoiceId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByInvoice(
            @PathVariable Long invoiceId) {
        List<PaymentResponse> responses = paymentService.getPaymentsByInvoice(invoiceId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get payments by booking")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsByBooking(
            @PathVariable Long bookingId) {
        List<PaymentResponse> responses = paymentService.getPaymentsByBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get all payments with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(Pageable pageable) {
        Page<PaymentResponse> responses = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get today's collection")
    @GetMapping("/collection/today")
    public ResponseEntity<ApiResponse<BigDecimal>> getTodayCollection() {
        BigDecimal collection = paymentService.getTodayCollection();
        return ResponseEntity.ok(ApiResponse.success(collection));
    }
    
    @Operation(summary = "Get payment method breakdown")
    @GetMapping("/breakdown")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getPaymentMethodBreakdown(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        Map<String, BigDecimal> breakdown = paymentService.getPaymentMethodBreakdown(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }
    
    @Operation(summary = "Get payments between dates")
    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getPaymentsBetweenDates(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        
        List<PaymentResponse> responses = paymentService.getPaymentsBetweenDates(start, end);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
