package com.hotel.reception.controller;

import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.InvoiceResponse;
import com.hotel.reception.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/v1/invoices")
@RequiredArgsConstructor
@Tag(name = "Invoice Management", description = "APIs for generating and managing invoices")

public class InvoiceController {
    
    private final InvoiceService invoiceService;
    
    @Operation(summary = "Generate invoice for booking")
    @PostMapping("/generate/{bookingId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> generateInvoice(@PathVariable Long bookingId) {
        InvoiceResponse response = invoiceService.generateInvoice(bookingId);
        return new ResponseEntity<>(
                ApiResponse.success("Invoice generated successfully", response),
                HttpStatus.CREATED
        );
    }
    
    @Operation(summary = "Get invoice by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get invoice by booking ID")
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByBooking(@PathVariable Long bookingId) {
        InvoiceResponse response = invoiceService.getInvoiceByBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get invoices by guest")
    @GetMapping("/guest/{guestId}")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByGuest(@PathVariable Long guestId) {
        List<InvoiceResponse> responses = invoiceService.getInvoicesByGuest(guestId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get all invoices with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoices(Pageable pageable) {
        Page<InvoiceResponse> responses = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get pending invoices")
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getPendingInvoices() {
        List<InvoiceResponse> responses = invoiceService.getPendingInvoices();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Update invoice status")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoiceStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        InvoiceResponse response = invoiceService.updateInvoiceStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Invoice status updated", response));
    }
    
    @Operation(summary = "Get total pending amount")
    @GetMapping("/pending/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPendingAmount() {
        BigDecimal total = invoiceService.getTotalPendingAmount();
        return ResponseEntity.ok(ApiResponse.success(total));
    }
}
