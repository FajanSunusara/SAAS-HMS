package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.InvoiceRequest;
import com.hotel.reception.model.dto.request.EmailRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.InvoiceResponse;
import com.hotel.reception.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @Operation(summary = "Search invoices by date range")
    @GetMapping("/search/by-date")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<InvoiceResponse> responses = invoiceService.getInvoicesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Search invoices by status")
    @GetMapping("/search/by-status")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(@RequestParam String status) {
        List<InvoiceResponse> responses = invoiceService.getInvoicesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Delete invoice")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.ok(ApiResponse.success("Invoice deleted successfully"));
        
        
    }
    
    @Operation(summary = "Get invoice by invoice number")
    @GetMapping("/by-number/{invoiceNumber}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByNumber(@PathVariable String invoiceNumber) {
        InvoiceResponse response = invoiceService.getInvoiceByNumber(invoiceNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update invoice")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InvoiceResponse>> updateInvoice(
            @PathVariable Long id,
            @RequestBody InvoiceRequest request) {
        InvoiceResponse response = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(ApiResponse.success("Invoice updated successfully", response));
    }

    @Operation(summary = "Send invoice via email")
    @PostMapping("/{id}/send-email")
    public ResponseEntity<ApiResponse<String>> sendInvoiceEmail(
            @PathVariable Long id,
            @RequestBody EmailRequest emailRequest) {
        invoiceService.sendInvoiceEmail(id, emailRequest);
        return ResponseEntity.ok(ApiResponse.success("Email sent successfully"));
    }

    @Operation(summary = "Generate PDF for invoice")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> generateInvoicePDF(@PathVariable Long id) {
        byte[] pdfBytes = invoiceService.generateInvoicePDF(id);
        
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }
    
    @Operation(summary = "Get all invoices with pagination (Optimized)")
    @GetMapping("/optimized")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getAllInvoicesOptimized(Pageable pageable) {
        Page<InvoiceResponse> responses = invoiceService.getAllInvoicesOptimized(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Get invoice by ID (Optimized)")
    @GetMapping("/{id}/optimized")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByIdOptimized(@PathVariable Long id) {
        InvoiceResponse response = invoiceService.getInvoiceByIdOptimized(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
