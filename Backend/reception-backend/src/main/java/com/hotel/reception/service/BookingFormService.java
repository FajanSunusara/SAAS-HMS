package com.hotel.reception.service;

import com.hotel.reception.model.dto.request.BookingFormRequest;
import com.hotel.reception.model.dto.request.GuestRequest;
import com.hotel.reception.model.dto.response.BookingFormResponse;
//import com.hotel.reception.model.dto.response.BookingResponse;
import com.hotel.reception.model.entity.*;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingFormService {
    
    private final BookingRepository bookingRepository;
    private final GuestRepository guestRepository;
    private final RoomRepository roomRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final ExtraServiceRepository extraServiceRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RoomPreferenceRepository roomPreferenceRepository;
    
//
//@Transactional
//public BookingFormResponse createBooking(BookingFormRequest request) {
//    // 🔹 Resolve or create guest
//    Optional<Guest> existingGuest = guestRepository.findExactGuest(
//        request.getFirstName().trim(),
//        request.getLastName().trim(),
//        request.getPhone().trim(),
//        request.getEmail()
//    );
//
//    Guest guest;
//    if (existingGuest.isPresent()) {
//        guest = existingGuest.get();
//    } else {
//        guest = new Guest();
//        guest.setFirstName(request.getFirstName().trim());
//        guest.setLastName(request.getLastName().trim());
//        guest.setPhone(request.getPhone().trim());
//        guest.setEmail(request.getEmail());
//        guest.setNationality(request.getNationality());
//        guest.setDateOfBirth(request.getDateOfBirth());
//        guest.setGender(request.getGender());
//        guest.setAddress(request.getAddress());
//        guest.setCity(request.getCity());
//        guest.setState(request.getState());
//        guest.setCountry(request.getCountry());
//        guest.setZipCode(request.getZipCode());
//        guest.setIdType(request.getIdType());
//        guest.setIdNumber(request.getIdNumber());
//        guest.setPassportNumber(request.getPassportNumber());
//        guest.setPassportExpiry(request.getPassportExpiry());
//        guest.setCompany(request.getCompany());
//        guest.setBusinessEmail(request.getBusinessEmail());
//        guest.setEmergencyContact(request.getEmergencyContact());
//        guest.setMarketingOptIn(request.getMarketingOptIn());
//        guest = guestRepository.save(guest);
//    }
//
//    // 🔹 Create booking entity
//    Booking booking = new Booking();
//    booking.setGuest(guest);
//    booking.setCheckInDate(request.getCheckInDate());
//    booking.setCheckOutDate(request.getCheckOutDate());
//    booking.setAdults(request.getAdults());
//    booking.setChildren(request.getChildren());
//    booking.setInfants(request.getInfants());
//    booking.setPurposeOfVisit(request.getPurposeOfVisit());
//    booking.setSpecialInstructions(request.getSpecialInstructions());
//    booking.setTermsAccepted(request.getTermsAccepted());
//    booking.setBookingSource(request.getBookingSource().toUpperCase());
//
//    // Calculate nights
//    booking.setNights((int) ChronoUnit.DAYS.between(
//        request.getCheckInDate(),
//        request.getCheckOutDate()
//    ));
//
//    // Generate a unique booking code
//    booking.setBookingCode(generateBookingCode());
//
//    // Set default values
//    booking.setStatus(BookingStatus.CONFIRMED);
//    booking.setPaymentStatus("PENDING");
//    booking.setTaxPercentage(new BigDecimal("10.00"));
//    booking.setIncludeTax(true);
//
//    // Save the booking
//    Booking savedBooking = bookingRepository.save(booking);
//
//    // Convert to response DTO
//    return convertToResponse(savedBooking);
//}
    
    @Transactional
    public BookingFormResponse createBooking(BookingFormRequest request) {
        // 1. Resolve or create guest
        Optional<Guest> existingGuest = guestRepository.findExactGuest(
            request.getFirstName().trim(),
            request.getLastName().trim(),
            request.getPhone().trim(),
            request.getEmail()
        );

        Guest guest;
        if (existingGuest.isPresent()) {
            guest = existingGuest.get();
        } else {
            guest = new Guest();
            guest.setFirstName(request.getFirstName().trim());
            guest.setLastName(request.getLastName().trim());
            guest.setPhone(request.getPhone().trim());
            guest.setEmail(request.getEmail());
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
            guest.setMarketingOptIn(request.getMarketingOptIn());
            guest = guestRepository.save(guest);
        }

        // 2. Create booking entity
        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setBookingType(request.getBookingType() != null ? request.getBookingType() : "SINGLE");
        booking.setBookingSource(request.getBookingSource() != null ? request.getBookingSource().toUpperCase() : "WALK_IN");
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setCheckInTime(request.getArrivalTime() != null ? request.getArrivalTime() : LocalTime.of(14, 0));
        booking.setCheckOutTime(request.getDepartureTime() != null ? request.getDepartureTime() : LocalTime.of(12, 0));
        booking.setAdults(request.getAdults() != null ? request.getAdults() : 1);
        booking.setChildren(request.getChildren() != null ? request.getChildren() : 0);
        booking.setInfants(request.getInfants() != null ? request.getInfants() : 0);
        booking.setPurposeOfVisit(request.getPurposeOfVisit());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setTermsAccepted(request.getTermsAccepted() != null && request.getTermsAccepted());

        // Calculate nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        booking.setNights((int) nights);

        // Set default status and payment
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("PENDING");

        // Tax and discount defaults (will be overwritten if provided in request)
        booking.setTaxPercentage(request.getTaxPercentage() != null ? request.getTaxPercentage() : new BigDecimal("10.00"));
        booking.setDiscountPercentage(request.getDiscountValue() != null && "percentage".equals(request.getDiscountType()) 
                ? request.getDiscountValue() : BigDecimal.ZERO);
        booking.setManualDiscount(request.getDiscountValue() != null && "fixed".equals(request.getDiscountType()) 
                ? request.getDiscountValue() : BigDecimal.ZERO);

        // Company details for company bookings
        if ("COMPANY".equalsIgnoreCase(request.getBookingType())) {
            booking.setCompanyName(request.getCompanyName());
            booking.setCompanyTaxId(request.getCompanyTaxId());
        }

        // Generate booking code
//        booking.setBookingCode(BookingCodeGenerator.generate());
        booking.setBookingCode(generateBookingCode());

        // Save initial booking (without rooms and amounts)
        Booking savedBooking = bookingRepository.save(booking);

        // 3. Handle room selection (assign rooms)
        if (request.getRoomSelectionMode() != null) {
            handleRoomSelection(request, savedBooking);
        }

        // 4. Create room preferences (if any)
        if (hasRoomPreferences(request)) {
            createRoomPreferences(request, savedBooking);
        }

        // 5. Update booking amounts (room charges, discounts, extra services, tax)
        updateBookingAmounts(savedBooking, request);

        // 6. Handle group members (if group booking)
        if ("GROUP".equalsIgnoreCase(request.getBookingType()) && request.getGroupMembers() != null) {
            addGroupMembers(savedBooking, request.getGroupMembers());
        }

        // 7. Refresh booking to get updated amounts and rooms
        Booking updatedBooking = bookingRepository.findById(savedBooking.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found after save"));

        // 8. Convert to response DTO
        return convertToResponse(updatedBooking);
    }

    // Helper to check if room preferences are present
    private boolean hasRoomPreferences(BookingFormRequest request) {
        return request.getBedPreference() != null ||
               request.getSmokingPreference() != null ||
               request.getFloorPreference() != null ||
               request.getViewPreference() != null ||
               request.getExtraBed() != null ||
               request.getCrib() != null ||
               request.getWheelchairAccess() != null ||
               request.getEarlyCheckIn() != null ||
               request.getLateCheckOut() != null;
    }

    // Add group members (helper method)
    private void addGroupMembers(Booking booking, List<GuestRequest> groupMemberRequests) {
        for (int i = 0; i < groupMemberRequests.size(); i++) {
            GuestRequest memberRequest = groupMemberRequests.get(i);
            // Create or find guest for each member
            Guest memberGuest = findOrCreateGuestFromRequest(memberRequest);
            GroupMember groupMember = new GroupMember();
            groupMember.setBooking(booking);
            groupMember.setGuest(memberGuest);
            groupMember.setIsLeader(i == 0); // First member is leader
            groupMemberRepository.save(groupMember);
        }
    }

    // Helper to create/find guest from a GuestRequest (for group members)
    private Guest findOrCreateGuestFromRequest(GuestRequest req) {
        return guestRepository.findByPhone(req.getPhone())
                .orElseGet(() -> {
                    Guest newGuest = new Guest();
                    newGuest.setFirstName(req.getFirstName());
                    newGuest.setLastName(req.getLastName());
                    newGuest.setEmail(req.getEmail());
                    newGuest.setPhone(req.getPhone());
                    newGuest.setNationality(req.getNationality());
                    newGuest.setIdType(req.getIdType());
                    newGuest.setIdNumber(req.getIdNumber());
                    newGuest.setDateOfBirth(req.getDateOfBirth());
                    // ... copy other fields as needed
                    return guestRepository.save(newGuest);
                });
    }

//booking.setBookingCode(generateBookingCode());

private String generateBookingCode() {
    return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 4);
}

    private String generateTemporaryBookingCode() {
        // Generate a temporary unique code until we have the actual booking ID
        return "TEMP-" + UUID.randomUUID().toString().substring(0, 8);
    }

//    private String generateBookingCode(Long bookingId) {
//        return "BK" + String.format("%08d", bookingId);
//    }
    
    private Guest findOrCreateGuest(BookingFormRequest request) {
        // Try to find existing guest by phone or email
        return guestRepository.findByPhone(request.getPhone())
                .orElseGet(() -> guestRepository.findByEmail(request.getEmail())
                        .orElseGet(() -> createNewGuest(request)));
    }
    
    private Guest createNewGuest(BookingFormRequest request) {
        Guest guest = new Guest();
        guest.setFirstName(request.getFirstName());
        guest.setLastName(request.getLastName());
        guest.setEmail(request.getEmail());
        guest.setPhone(request.getPhone());
        guest.setNationality(request.getNationality());
        guest.setAddress(request.getAddress());
        guest.setIdType(request.getIdType());
        guest.setIdNumber(request.getIdNumber());
        guest.setDateOfBirth(request.getDateOfBirth());
        guest.setGender(request.getGender());
        guest.setPassportNumber(request.getPassportNumber());
        guest.setPassportExpiry(request.getPassportExpiry());
        guest.setCountry(request.getCountry());
        guest.setState(request.getState());
        guest.setCity(request.getCity());
        guest.setZipCode(request.getZipCode());
        guest.setEmergencyContact(request.getEmergencyContact());
        guest.setCompany(request.getCompany());
        guest.setBusinessEmail(request.getBusinessEmail());
        guest.setLoyaltyNumber(request.getLoyaltyNumber());
        guest.setCreatedBy("SYSTEM");
        
        return guestRepository.save(guest);
    }
    
    private Booking createBookingEntity(BookingFormRequest request, Guest guest) {
        Booking booking = new Booking();
        booking.setGuest(guest);
        booking.setBookingType("SINGLE");
        booking.setBookingSource(request.getBookingSource().toUpperCase());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setCheckInTime(request.getArrivalTime());
        booking.setCheckOutTime(request.getDepartureTime());
        
        // Calculate nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        booking.setNights((int) nights);
        
        booking.setAdults(request.getAdults());
        booking.setChildren(request.getChildren());
        booking.setInfants(request.getInfants());
        booking.setPurposeOfVisit(request.getPurposeOfVisit());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus("PENDING");
        booking.setTermsAccepted(request.getTermsAccepted());
        booking.setCreatedBy("SYSTEM");
        
        return booking;
    }
    
    private void handleRoomSelection(BookingFormRequest request, Booking booking) {
        if ("type".equals(request.getRoomSelectionMode())) {
            // Room type wise selection
            for (BookingFormRequest.RoomTypeSelection roomType : request.getSelectedRoomTypes()) {
                // Find available rooms of this type
                List<Room> availableRooms = roomRepository.findAvailableRoomsByType(
                    booking.getCheckInDate(),
                    booking.getCheckOutDate(),
                    getRoomTypeName(roomType.getTypeId())
                );
                
                // Assign specified number of rooms
                for (int i = 0; i < Math.min(roomType.getNumberOfRooms(), availableRooms.size()); i++) {
                    Room room = availableRooms.get(i);
                    assignRoomToBooking(room, booking, booking.getGuest());
                }
            }
        } else {
            // Room number wise selection
            for (String roomNumber : request.getSelectedRooms()) {
                Room room = roomRepository.findByRoomNumber(roomNumber)
                        .orElseThrow(() -> new RuntimeException("Room not found: " + roomNumber));
                
                // Check if room is available
                if (!isRoomAvailable(room, booking.getCheckInDate(), booking.getCheckOutDate())) {
                    throw new RuntimeException("Room " + roomNumber + " is not available for selected dates");
                }
                
                assignRoomToBooking(room, booking, booking.getGuest());
            }
        }
    }
    
    private void assignRoomToBooking(Room room, Booking booking, Guest guest) {
        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBooking(booking);
        bookingRoom.setRoom(room);
        bookingRoom.setRoomRate(room.getBaseRate());
        bookingRoom.setGuest(guest);
        bookingRoomRepository.save(bookingRoom);
        
        // Update room status
        room.setStatus("RESERVED");
        roomRepository.save(room);
    }
    
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        List<BookingRoom> existingBookings = bookingRoomRepository.findBookingsForRoom(
            room.getRoomId(), checkIn, checkOut
        );
        return existingBookings.isEmpty();
    }
    
    private String getRoomTypeName(Long typeId) {
        // Map typeId to room type name
        Map<Long, String> roomTypeMap = Map.of(
            1L, "Standard Room",
            2L, "Deluxe Room",
            3L, "Executive Suite",
            4L, "Presidential Suite"
        );
        return roomTypeMap.getOrDefault(typeId, "Standard Room");
    }
    
    private void createRoomPreferences(BookingFormRequest request, Booking booking) {
        RoomPreference preference = new RoomPreference();
        preference.setBooking(booking);
        preference.setBedPreference(request.getBedPreference());
        preference.setSmokingPreference(request.getSmokingPreference().toUpperCase());
        preference.setFloorPreference(request.getFloorPreference());
        preference.setViewPreference(request.getViewPreference());
        preference.setExtraBed(request.getExtraBed());
        preference.setCrib(request.getCrib());
        preference.setWheelchairAccess(request.getWheelchairAccess());
        preference.setEarlyCheckIn(request.getEarlyCheckIn());
        preference.setLateCheckOut(request.getLateCheckOut());
        
        roomPreferenceRepository.save(preference);
    }
    
    private void updateBookingAmounts(Booking booking, BookingFormRequest request) {
        // Calculate base price from assigned rooms
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        BigDecimal basePrice = bookingRooms.stream()
                .map(BookingRoom::getRoomRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(BigDecimal.valueOf(booking.getNights()));
        
        booking.setBasePrice(basePrice);
        
        // Calculate discount
        BigDecimal discountAmount = BigDecimal.ZERO;
        if ("percentage".equals(request.getDiscountType())) {
            discountAmount = basePrice.multiply(request.getDiscountValue())
                    .divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = request.getDiscountValue();
        }
        booking.setManualDiscount(discountAmount);
        
        // Calculate extra services
        BigDecimal extraServicesAmount = calculateExtraServices(request.getExtraServices(), booking);
        
        // Calculate tax
        BigDecimal taxableAmount = basePrice.subtract(discountAmount).add(extraServicesAmount);
        BigDecimal taxAmount = taxableAmount.multiply(request.getTaxPercentage())
                .divide(BigDecimal.valueOf(100));
        
        // Calculate total
        BigDecimal totalAmount = taxableAmount.add(taxAmount);
        
        // Update booking
        booking.setTaxPercentage(request.getTaxPercentage());
        bookingRepository.save(booking);
    }
    
    private BigDecimal calculateExtraServices(List<Long> serviceIds, Booking booking) {
        if (serviceIds == null || serviceIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = BigDecimal.ZERO;
        List<ExtraService> services = extraServiceRepository.findAllById(serviceIds);
        
        for (ExtraService service : services) {
            // Calculate based on service type and booking details
            BigDecimal serviceTotal = service.getPrice();
            
            // For services charged per night
            if (service.getUnit() != null && service.getUnit().contains("per night")) {
                serviceTotal = serviceTotal.multiply(BigDecimal.valueOf(booking.getNights()));
            }
            
            // For services charged per person
            if (service.getUnit() != null && service.getUnit().contains("per person")) {
                int guests = booking.getAdults() + booking.getChildren();
                serviceTotal = serviceTotal.multiply(BigDecimal.valueOf(guests));
            }
            
            total = total.add(serviceTotal);
        }
        
        return total;
    }
    
    private String generateBookingCode(Long bookingId) {
        return "BK" + String.format("%08d", bookingId);
    }
    
    private BookingFormResponse convertToResponse(Booking booking) {
        BookingFormResponse response = new BookingFormResponse();
        response.setBookingId(booking.getBookingId());
        response.setBookingCode(booking.getBookingCode());
        response.setBookingType(booking.getBookingType());
        response.setBookingSource(booking.getBookingSource());
        response.setStatus(booking.getStatus().name());
        response.setPaymentStatus(booking.getPaymentStatus());
        
        // Guest info
        if (booking.getGuest() != null) {
            BookingFormResponse.GuestResponse guestResponse = new BookingFormResponse.GuestResponse();
            guestResponse.setGuestId(booking.getGuest().getGuestId());
            guestResponse.setFirstName(booking.getGuest().getFirstName());
            guestResponse.setLastName(booking.getGuest().getLastName());
            guestResponse.setEmail(booking.getGuest().getEmail());
            guestResponse.setPhone(booking.getGuest().getPhone());
            guestResponse.setNationality(booking.getGuest().getNationality());
            response.setGuest(guestResponse);
        }
        
        // Stay info
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setCheckInTime(booking.getCheckInTime());
        response.setCheckOutTime(booking.getCheckOutTime());
        response.setActualCheckIn(booking.getActualCheckIn());
        response.setActualCheckOut(booking.getActualCheckOut());
        response.setNights(booking.getNights());
        response.setAdults(booking.getAdults());
        response.setChildren(booking.getChildren());
        response.setInfants(booking.getInfants());
        response.setPurposeOfVisit(booking.getPurposeOfVisit());
        response.setSpecialInstructions(booking.getSpecialInstructions());
        
        // Room info
        List<BookingFormResponse.RoomResponse> roomResponses = new ArrayList<>();
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        
        for (BookingRoom bookingRoom : bookingRooms) {
            BookingFormResponse.RoomResponse roomResponse = new BookingFormResponse.RoomResponse();
            roomResponse.setRoomNumber(bookingRoom.getRoom().getRoomNumber());
            roomResponse.setRoomType(bookingRoom.getRoom().getRoomType());
            roomResponse.setFloorNumber(bookingRoom.getRoom().getFloorNumber());
            roomResponse.setRate(bookingRoom.getRoomRate());
            roomResponse.setStatus(bookingRoom.getRoom().getStatus());
            
            if (bookingRoom.getRoom().getFeatures() != null) {
                roomResponse.setAmenities(Arrays.asList(bookingRoom.getRoom().getFeatures()));
            }
            
            if (bookingRoom.getGuest() != null) {
                BookingFormResponse.GuestResponse assignedGuest = new BookingFormResponse.GuestResponse();
                assignedGuest.setGuestId(bookingRoom.getGuest().getGuestId());
                assignedGuest.setFirstName(bookingRoom.getGuest().getFirstName());
                assignedGuest.setLastName(bookingRoom.getGuest().getLastName());
                roomResponse.setAssignedGuest(assignedGuest);
            }
            
            roomResponses.add(roomResponse);
        }
        
        response.setRooms(roomResponses);
        response.setTotalRooms(roomResponses.size());
        
        // Pricing info
        response.setBasePrice(booking.getBasePrice());
        response.setDiscountAmount(booking.getManualDiscount());
        
        // Calculate totals
        BigDecimal basePrice = booking.getBasePrice() != null ? booking.getBasePrice() : BigDecimal.ZERO;
        BigDecimal discountAmount = booking.getManualDiscount() != null ? booking.getManualDiscount() : BigDecimal.ZERO;
        BigDecimal taxableAmount = basePrice.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount.multiply(booking.getTaxPercentage())
                .divide(BigDecimal.valueOf(100));
        BigDecimal totalAmount = taxableAmount.add(taxAmount);
        
        response.setTaxAmount(taxAmount);
        response.setTotalAmount(totalAmount);
        
        // Timestamps
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        return response;
    }
    
    public BookingFormResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        return convertToResponse(booking);
    }
    
    public BookingFormResponse getBookingByCode(String code) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .orElseThrow(() -> new RuntimeException("Booking not found with code: " + code));
        return convertToResponse(booking);
    }
}