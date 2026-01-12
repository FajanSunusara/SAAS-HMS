package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.GuestRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.CurrentGuestResponse;
import com.hotel.reception.model.dto.response.GuestHistoryResponse;
import com.hotel.reception.model.dto.response.GuestResponse;
import com.hotel.reception.model.dto.response.GuestStatsResponse;
import com.hotel.reception.model.dto.response.GuestSummaryStats;
import com.hotel.reception.service.GuestService;
import com.hotel.reception.service.GuestSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/guests")
@RequiredArgsConstructor
@Tag(name = "Guest Management", description = "APIs for managing hotel guests")

public class GuestController {
    
    private final GuestService guestService;
    private final GuestSearchService guestSearchService ;
    
    
    @Operation(summary = "Create new guest", description = "Register a new guest in the system")
    @PostMapping
    public ResponseEntity<ApiResponse<GuestResponse>> createGuest(@Valid @RequestBody GuestRequest request) {
        GuestResponse response = guestService.createGuest(request);
        return new ResponseEntity<>(
                ApiResponse.success("Guest created successfully", response),
                HttpStatus.CREATED
        );
    }
    
    @Operation(summary = "Get guest by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestResponse>> getGuestById(@PathVariable Long id) {
        GuestResponse response = guestService.getGuestById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Search guests by keyword")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<GuestResponse>>> searchGuests(
            @RequestParam String keyword) {
        List<GuestResponse> responses = guestService.searchGuests(keyword);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Search guests with pagination")
    @GetMapping("/search/paginated")
    public ResponseEntity<ApiResponse<Page<GuestResponse>>> searchGuestsWithPagination(
            @RequestParam String keyword,
            Pageable pageable) {
        Page<GuestResponse> responses = guestService.searchGuestsWithPagination(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get all guests")
    @GetMapping
    public ResponseEntity<ApiResponse<List<GuestResponse>>> getAllGuests() {
        List<GuestResponse> responses = guestService.getAllGuests();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Update guest information")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GuestResponse>> updateGuest(
            @PathVariable Long id,
            @Valid @RequestBody GuestRequest request) {
        GuestResponse response = guestService.updateGuest(id, request);
        return ResponseEntity.ok(ApiResponse.success("Guest updated successfully", response));
    }
    
    @Operation(summary = "Delete guest")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGuest(@PathVariable Long id) {
        guestService.deleteGuest(id);
        return ResponseEntity.ok(ApiResponse.success("Guest deleted successfully", null));
    }
    
    @Operation(summary = "Find guest by email")
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<GuestResponse>> findByEmail(@PathVariable String email) {
        GuestResponse response = guestService.findByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Find guest by phone")
    @GetMapping("/phone/{phone}")
    public ResponseEntity<ApiResponse<GuestResponse>> findByPhone(@PathVariable String phone) {
        GuestResponse response = guestService.findByPhone(phone);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Guest API is running!");
    }
    
    
    @Operation(summary = "Search current guests")
    @GetMapping("/current")
    public ResponseEntity<ApiResponse<Page<CurrentGuestResponse>>> getCurrentGuests(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
 
		Page<CurrentGuestResponse> responses = guestSearchService.searchCurrentGuests(
            keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Search all guests with history")
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<GuestHistoryResponse>>> getAllGuestsWithHistory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? 
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GuestHistoryResponse> responses = guestSearchService.searchAllGuestsWithHistory(
            keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get guest dashboard statistics")
    @GetMapping("/stats/dashboard")
    public ResponseEntity<ApiResponse<GuestStatsResponse>> getDashboardStats() {
        GuestStatsResponse stats = guestSearchService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    @Operation(summary = "Get guest statistics")
    @GetMapping("/stats/summary")
    public ResponseEntity<ApiResponse<GuestSummaryStats>> getGuestSummaryStats() {
        GuestSummaryStats summary = guestSearchService.getGuestSummaryStats();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
}
