package com.hotel.reception.controller;

import com.hotel.reception.model.entity.Room;
import com.hotel.reception.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    
    @Autowired
    private RoomRepository roomRepository;
    
    // GET all rooms
    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return ResponseEntity.ok(rooms);
    }
    
    // GET room by ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isPresent()) {
            return ResponseEntity.ok(room.get());
        } else {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Room not found with ID: " + id);
            return ResponseEntity.status(404).body(error);
        }
    }
    
    // GET available rooms
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms() {
        List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE");
        return ResponseEntity.ok(availableRooms);
    }
    
    // POST create new room
    @PostMapping
    public ResponseEntity<?> createRoom(@RequestBody Room roomRequest) {
        try {
            // Check if room number already exists
            Optional<Room> existingRoom = roomRepository.findByRoomNumber(roomRequest.getRoomNumber());
            if (existingRoom.isPresent()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room with number already exists: " + roomRequest.getRoomNumber());
                return ResponseEntity.status(400).body(error);
            }
            
            // Set default values if not provided
            if (roomRequest.getStatus() == null) {
                roomRequest.setStatus("AVAILABLE");
            }
            if (roomRequest.getBaseRate() == null) {
                roomRequest.setBaseRate(BigDecimal.valueOf(5000.00));
            }
            if (roomRequest.getCapacity() == null) {
                roomRequest.setCapacity(2);
            }
            
            // Set timestamps
            roomRequest.setCreatedAt(LocalDateTime.now());
            roomRequest.setUpdatedAt(LocalDateTime.now());
            
            // Save to database
            Room savedRoom = roomRepository.save(roomRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Room created successfully");
            response.put("roomId", savedRoom.getRoomId());
            response.put("room", savedRoom);
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to create room: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // PUT update room
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Room roomUpdates) {
        try {
            Optional<Room> existingRoom = roomRepository.findById(id);
            if (existingRoom.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            Room room = existingRoom.get();
            
            // Update fields if provided
            if (roomUpdates.getRoomNumber() != null) room.setRoomNumber(roomUpdates.getRoomNumber());
            if (roomUpdates.getRoomType() != null) room.setRoomType(roomUpdates.getRoomType());
            if (roomUpdates.getFloorNumber() != null) room.setFloorNumber(roomUpdates.getFloorNumber());
            if (roomUpdates.getBaseRate() != null) room.setBaseRate(roomUpdates.getBaseRate());
            if (roomUpdates.getStatus() != null) room.setStatus(roomUpdates.getStatus());
            if (roomUpdates.getCapacity() != null) room.setCapacity(roomUpdates.getCapacity());
            if (roomUpdates.getFeatures() != null) room.setFeatures(roomUpdates.getFeatures());
            if (roomUpdates.getDescription() != null) room.setDescription(roomUpdates.getDescription());
            if (roomUpdates.getSmoking() != null) room.setSmoking(roomUpdates.getSmoking());
            if (roomUpdates.getAccessible() != null) room.setAccessible(roomUpdates.getAccessible());
            if (roomUpdates.getVip() != null) room.setVip(roomUpdates.getVip());
            
            room.setUpdatedAt(LocalDateTime.now());
            
            Room updatedRoom = roomRepository.save(room);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Room updated successfully");
            response.put("room", updatedRoom);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update room: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // DELETE room
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        try {
            Optional<Room> room = roomRepository.findById(id);
            if (room.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Room not found with ID: " + id);
                return ResponseEntity.status(404).body(error);
            }
            
            roomRepository.deleteById(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Room deleted successfully");
            response.put("roomId", id.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete room: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
    
    // GET room statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getRoomStatistics() {
        try {
            long totalRooms = roomRepository.count();
            long availableRooms = roomRepository.countByStatus("AVAILABLE");
            long occupiedRooms = roomRepository.countByStatus("OCCUPIED");
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRooms", totalRooms);
            stats.put("availableRooms", availableRooms);
            stats.put("occupiedRooms", occupiedRooms);
            stats.put("occupancyRate", totalRooms > 0 ? 
                (double) occupiedRooms / totalRooms * 100 : 0);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get room statistics: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}