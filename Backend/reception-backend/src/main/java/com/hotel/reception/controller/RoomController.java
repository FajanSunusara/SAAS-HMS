package com.hotel.reception.controller;

import com.hotel.reception.model.dto.request.RoomRequest;
import com.hotel.reception.model.dto.response.ApiResponse;
import com.hotel.reception.model.dto.response.RoomResponse;
import com.hotel.reception.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/rooms")
@RequiredArgsConstructor
@Tag(name = "Room Management", description = "APIs for managing hotel rooms with Redis caching")

public class RoomController {
    
    private final RoomService roomService;
    
    @Operation(summary = "Create new room")
    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@Valid @RequestBody RoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return new ResponseEntity<>(
                ApiResponse.success("Room created successfully", response),
                HttpStatus.CREATED
        );
    }
    
    @Operation(summary = "Get room by ID (Redis cached)")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(@PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @Operation(summary = "Get all rooms (Redis cached)")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAllRooms() {
        List<RoomResponse> responses = roomService.getAllRooms();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get rooms by status (Redis cached)")
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getRoomsByStatus(@PathVariable String status) {
        List<RoomResponse> responses = roomService.getRoomsByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Get available rooms (Redis cached)")
    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) String roomType) {
        
        List<RoomResponse> responses = roomService.getAvailableRooms(checkIn, checkOut, roomType);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
    
    @Operation(summary = "Update room status (triggers cache eviction & Redis Pub/Sub)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoomStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        
        RoomResponse response = roomService.updateRoomStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Room status updated and broadcasted", response));
    }
    
    @Operation(summary = "Update room details")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody RoomRequest request) {
        
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }
    
    @Operation(summary = "Delete room")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("Room deleted successfully", null));
    }
    
    @Operation(summary = "Get room status summary")
    @GetMapping("/status/summary")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getRoomStatusSummary() {
        Map<String, Long> summary = new HashMap<>();
        summary.put("TOTAL", roomService.getTotalRoomCount());
        summary.put("AVAILABLE", roomService.getCountByStatus("AVAILABLE"));
        summary.put("OCCUPIED", roomService.getCountByStatus("OCCUPIED"));
        summary.put("RESERVED", roomService.getCountByStatus("RESERVED"));
        summary.put("MAINTENANCE", roomService.getCountByStatus("MAINTENANCE"));
        summary.put("CLEANING", roomService.getCountByStatus("CLEANING"));
        
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    @Operation(summary = "Health check")
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Room API is running with Redis!");
    }
}
