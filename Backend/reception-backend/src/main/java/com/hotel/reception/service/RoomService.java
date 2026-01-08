package com.hotel.reception.service;

import com.hotel.reception.events.RoomEvent;
import com.hotel.reception.events.RoomEventPublisher;
import com.hotel.reception.exception.ResourceNotFoundException;
import com.hotel.reception.model.dto.request.RoomRequest;
import com.hotel.reception.model.dto.response.RoomResponse;
import com.hotel.reception.model.entity.Room;
import com.hotel.reception.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RoomService {
    
    private final RoomRepository roomRepository;
    private final RoomEventPublisher roomEventPublisher;
    
    public RoomResponse createRoom(RoomRequest request) {
        log.info("Creating new room: {}", request.getRoomNumber());
        
        if (roomRepository.findByRoomNumber(request.getRoomNumber()).isPresent()) {
            throw new RuntimeException("Room number already exists: " + request.getRoomNumber());
        }
        
        Room room = mapToEntity(request);
        Room savedRoom = roomRepository.save(room);
        
        log.info("Room created successfully: {}", savedRoom.getRoomNumber());
        return mapToResponse(savedRoom);
    }
    
    @Cacheable(value = "room", key = "#roomId")
    public RoomResponse getRoomById(Long roomId) {
        log.debug("Fetching room by ID: {}", roomId);
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        return mapToResponse(room);
    }
    
    public Room getRoomEntityById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
    }
    
    @Cacheable(value = "rooms", key = "'all'")
    public List<RoomResponse> getAllRooms() {
        log.debug("Fetching all rooms (cached)");
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "roomsByStatus", key = "#status")
    public List<RoomResponse> getRoomsByStatus(String status) {
        log.debug("Fetching rooms by status: {}", status);
        return roomRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Cacheable(value = "availableRooms", key = "#checkIn + '_' + #checkOut + '_' + #roomType")
    public List<RoomResponse> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, String roomType) {
        log.debug("Fetching available rooms from {} to {} for type: {}", checkIn, checkOut, roomType);
        return roomRepository.findAvailableRooms(checkIn, checkOut, roomType).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    @Caching(evict = {
        @CacheEvict(value = "room", key = "#roomId"),
        @CacheEvict(value = "rooms", allEntries = true),
        @CacheEvict(value = "roomsByStatus", allEntries = true),
        @CacheEvict(value = "availableRooms", allEntries = true)
    })
    public RoomResponse updateRoomStatus(Long roomId, String newStatus) {
        log.info("Updating room {} status to {}", roomId, newStatus);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        String oldStatus = room.getStatus();
        room.setStatus(newStatus);
        Room updatedRoom = roomRepository.save(room);
        
        // Publish Redis event for real-time updates
        RoomEvent event = RoomEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ROOM_UPDATED")
                .roomId(roomId)
                .roomNumber(room.getRoomNumber())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .updatedAt(LocalDateTime.now())
                .build();
        
        roomEventPublisher.publishRoomUpdated(event);
        
        log.info("Room status updated and event published: {}", roomId);
        return mapToResponse(updatedRoom);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "room", key = "#roomId"),
        @CacheEvict(value = "rooms", allEntries = true),
        @CacheEvict(value = "roomsByStatus", allEntries = true)
    })
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        log.info("Updating room: {}", roomId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        updateRoomFields(room, request);
        Room updatedRoom = roomRepository.save(room);
        
        log.info("Room updated successfully: {}", roomId);
        return mapToResponse(updatedRoom);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "room", key = "#roomId"),
        @CacheEvict(value = "rooms", allEntries = true),
        @CacheEvict(value = "roomsByStatus", allEntries = true),
        @CacheEvict(value = "availableRooms", allEntries = true)
    })
    public void deleteRoom(Long roomId) {
        log.info("Deleting room: {}", roomId);
        
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));
        
        roomRepository.delete(room);
        log.info("Room deleted successfully: {}", roomId);
    }
    
    public Long getTotalRoomCount() {
        return roomRepository.getTotalRoomCount();
    }
    
    public Long getCountByStatus(String status) {
        return roomRepository.countByStatus(status);
    }
    
    // Helper methods
    private Room mapToEntity(RoomRequest request) {
        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setFloorNumber(request.getFloorNumber());
        room.setBaseRate(request.getBaseRate());
        room.setStatus(request.getStatus() != null ? request.getStatus() : "AVAILABLE");
        room.setCapacity(request.getCapacity() != null ? request.getCapacity() : 2);
        room.setFeatures(request.getFeatures());
        room.setDescription(request.getDescription());
        room.setSmoking(request.getSmoking() != null ? request.getSmoking() : false);
        room.setAccessible(request.getAccessible() != null ? request.getAccessible() : false);
        room.setVip(request.getVip() != null ? request.getVip() : false);
        return room;
    }
    
    private void updateRoomFields(Room room, RoomRequest request) {
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        room.setFloorNumber(request.getFloorNumber());
        room.setBaseRate(request.getBaseRate());
        room.setStatus(request.getStatus());
        room.setCapacity(request.getCapacity());
        room.setFeatures(request.getFeatures());
        room.setDescription(request.getDescription());
        room.setSmoking(request.getSmoking());
        room.setAccessible(request.getAccessible());
        room.setVip(request.getVip());
    }
    
    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .floorNumber(room.getFloorNumber())
                .baseRate(room.getBaseRate())
                .status(room.getStatus())
                .capacity(room.getCapacity())
                .features(room.getFeatures())
                .description(room.getDescription())
                .smoking(room.getSmoking())
                .accessible(room.getAccessible())
                .vip(room.getVip())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }
}
