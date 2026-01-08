package com.hotel.reception.service;

import com.hotel.reception.exception.ResourceNotFoundException;
import com.hotel.reception.model.dto.request.GuestRequest;
import com.hotel.reception.model.dto.response.GuestResponse;
import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.repository.GuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GuestService {
    
    private final GuestRepository guestRepository;
    
    public GuestResponse createGuest(GuestRequest request) {
        log.info("Creating new guest: {} {}", request.getFirstName(), request.getLastName());
        
        // Check for duplicates
        if (request.getEmail() != null && guestRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Guest with email already exists: " + request.getEmail());
        }
        
        if (guestRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Guest with phone already exists: " + request.getPhone());
        }
        
        Guest guest = mapToEntity(request);
        Guest savedGuest = guestRepository.save(guest);
        
        log.info("Guest created successfully with ID: {}", savedGuest.getGuestId());
        return mapToResponse(savedGuest);
    }
    
    @Cacheable(value = "guest", key = "#guestId")
    public GuestResponse getGuestById(Long guestId) {
        log.debug("Fetching guest by ID: {}", guestId);
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
        return mapToResponse(guest);
    }
    
    public Guest getGuestEntityById(Long guestId) {
        return guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
    }
    
    @Cacheable(value = "guests", key = "#keyword")
    public List<GuestResponse> searchGuests(String keyword) {
        log.debug("Searching guests with keyword: {}", keyword);
        List<Guest> guests = guestRepository.searchGuests(keyword);
        return guests.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<GuestResponse> searchGuestsWithPagination(String keyword, Pageable pageable) {
        log.debug("Searching guests with keyword: {} and pagination", keyword);
        Page<Guest> guestPage = guestRepository.searchGuestsWithPagination(keyword, pageable);
        return guestPage.map(this::mapToResponse);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "guest", key = "#guestId"),
        @CacheEvict(value = "guests", allEntries = true)
    })
    public GuestResponse updateGuest(Long guestId, GuestRequest request) {
        log.info("Updating guest with ID: {}", guestId);
        
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
        
        // Check for duplicate email (excluding current guest)
        if (request.getEmail() != null) {
            guestRepository.findByEmail(request.getEmail())
                    .ifPresent(existingGuest -> {
                        if (!existingGuest.getGuestId().equals(guestId)) {
                            throw new RuntimeException("Email already in use by another guest");
                        }
                    });
        }
        
        // Check for duplicate phone (excluding current guest)
        guestRepository.findByPhone(request.getPhone())
                .ifPresent(existingGuest -> {
                    if (!existingGuest.getGuestId().equals(guestId)) {
                        throw new RuntimeException("Phone already in use by another guest");
                    }
                });
        
        updateGuestFields(guest, request);
        Guest updatedGuest = guestRepository.save(guest);
        
        log.info("Guest updated successfully: {}", guestId);
        return mapToResponse(updatedGuest);
    }
    
    @Caching(evict = {
        @CacheEvict(value = "guest", key = "#guestId"),
        @CacheEvict(value = "guests", allEntries = true)
    })
    public void deleteGuest(Long guestId) {
        log.info("Deleting guest with ID: {}", guestId);
        
        Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + guestId));
        
        guestRepository.delete(guest);
        log.info("Guest deleted successfully: {}", guestId);
    }
    
    public List<GuestResponse> getAllGuests() {
        log.debug("Fetching all guests");
        return guestRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public GuestResponse findByEmail(String email) {
        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with email: " + email));
        return mapToResponse(guest);
    }
    
    public GuestResponse findByPhone(String phone) {
        Guest guest = guestRepository.findByPhone(phone)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with phone: " + phone));
        return mapToResponse(guest);
    }
    
    // Helper methods
    private Guest mapToEntity(GuestRequest request) {
        Guest guest = new Guest();
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setCountryCode(request.getCountryCode() != null ? request.getCountryCode() : "+91");
        guest.setNationality(request.getNationality());
        guest.setDateOfBirth(request.getDateOfBirth());
        guest.setGender(request.getGender());
        guest.setAddress(request.getAddress());
        guest.setCity(request.getCity());
        guest.setState(request.getState());
        guest.setCountry(request.getCountry());
        guest.setZipCode(request.getZipCode());
        guest.setIdType(request.getIdType());
        guest.setIdNumber(request.getIdNumber());
        guest.setPassportNumber(request.getPassportNumber());
        guest.setPassportExpiry(request.getPassportExpiry());
        guest.setCompany(request.getCompany());
        guest.setBusinessEmail(request.getBusinessEmail());
        guest.setEmergencyContact(request.getEmergencyContact());
        guest.setSpecialRequests(request.getSpecialRequests());
        guest.setMarketingOptIn(request.getMarketingOptIn() != null ? request.getMarketingOptIn() : false);
        return guest;
    }
    
    private void updateGuestFields(Guest guest, GuestRequest request) {
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setCountryCode(request.getCountryCode());
        guest.setNationality(request.getNationality());
        guest.setDateOfBirth(request.getDateOfBirth());
        guest.setGender(request.getGender());
        guest.setAddress(request.getAddress());
        guest.setCity(request.getCity());
        guest.setState(request.getState());
        guest.setCountry(request.getCountry());
        guest.setZipCode(request.getZipCode());
        guest.setIdType(request.getIdType());
        guest.setIdNumber(request.getIdNumber());
        guest.setPassportNumber(request.getPassportNumber());
        guest.setPassportExpiry(request.getPassportExpiry());
        guest.setCompany(request.getCompany());
        guest.setBusinessEmail(request.getBusinessEmail());
        guest.setEmergencyContact(request.getEmergencyContact());
        guest.setSpecialRequests(request.getSpecialRequests());
        guest.setMarketingOptIn(request.getMarketingOptIn());
    }
    
    private GuestResponse mapToResponse(Guest guest) {
        return GuestResponse.builder()
                .guestId(guest.getGuestId())
                .firstName(guest.getFirstName())
                .lastName(guest.getLastName())
                .fullName(guest.getFirstName() + " " + guest.getLastName())
                .email(guest.getEmail())
                .phone(guest.getPhone())
                .countryCode(guest.getCountryCode())
                .nationality(guest.getNationality())
                .dateOfBirth(guest.getDateOfBirth())
                .gender(guest.getGender())
                .address(guest.getAddress())
                .city(guest.getCity())
                .state(guest.getState())
                .country(guest.getCountry())
                .zipCode(guest.getZipCode())
                .idType(guest.getIdType())
                .idNumber(guest.getIdNumber())
                .passportNumber(guest.getPassportNumber())
                .passportExpiry(guest.getPassportExpiry())
                .loyaltyNumber(guest.getLoyaltyNumber())
                .vipStatus(guest.getVipStatus())
                .company(guest.getCompany())
                .createdAt(guest.getCreatedAt())
                .status("ACTIVE")
                .build();
    }
}
