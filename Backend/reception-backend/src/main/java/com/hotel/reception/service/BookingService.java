package com.hotel.reception.service;

import com.hotel.reception.exception.*;
import com.hotel.reception.model.dto.request.BookingRequest;
import com.hotel.reception.model.dto.request.CheckoutRequest;
import com.hotel.reception.model.dto.request.GuestRequest;
import com.hotel.reception.model.dto.response.*;
import com.hotel.reception.model.entity.*;
import com.hotel.reception.model.enums.BookingStatus;
import com.hotel.reception.repository.*;
import com.hotel.reception.util.BookingCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageImpl;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    private final RoomService roomService;
    private final BookingRoomRepository bookingRoomRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RoomPreferenceRepository roomPreferenceRepository;
    private final RoomRepository roomRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating new booking of type: {}", request.getBookingType());
        
        // Validate dates
        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());
        
        // Get or create guest
        Guest primaryGuest = getOrCreateGuest(request);
        
        // Validate room availability
        validateRoomAvailability(request.getRoomIds(), request.getCheckInDate(), request.getCheckOutDate());
        
        // Create booking
        Booking booking = new Booking();
        booking.setBookingCode(BookingCodeGenerator.generate());
        booking.setGuest(primaryGuest);
        booking.setBookingType(request.getBookingType());
        booking.setBookingSource(request.getBookingSource() != null ? request.getBookingSource() : "WALK_IN");
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setCheckInTime(request.getCheckInTime());
        booking.setCheckOutTime(request.getCheckOutTime());
        
        // Calculate nights
        long nights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        booking.setNights((int) nights);
        
        booking.setAdults(request.getAdults() != null ? request.getAdults() : 1);
        booking.setChildren(request.getChildren() != null ? request.getChildren() : 0);
        booking.setInfants(request.getInfants() != null ? request.getInfants() : 0);
        booking.setPurposeOfVisit(request.getPurposeOfVisit());
        booking.setSpecialInstructions(request.getSpecialInstructions());
        booking.setStatus(BookingStatus.CONFIRMED.name());
        booking.setPaymentStatus("PENDING");
        
        // Set pricing
        booking.setDiscountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO);
        booking.setManualDiscount(request.getManualDiscount() != null ? request.getManualDiscount() : BigDecimal.ZERO);
        booking.setTaxPercentage(request.getTaxPercentage() != null ? request.getTaxPercentage() : new BigDecimal("10.00"));
        booking.setIncludeTax(request.getIncludeTax() != null ? request.getIncludeTax() : true);
        booking.setTermsAccepted(request.getTermsAccepted() != null ? request.getTermsAccepted() : false);
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Assign rooms
        assignRoomsToBooking(savedBooking, request.getRoomIds());
        
        // Handle group booking
        if ("GROUP".equals(request.getBookingType()) && request.getGroupMembers() != null) {
            addGroupMembers(savedBooking, request.getGroupMembers());
        }
        
        // Add room preferences
        if (request.getPreferences() != null) {
            addRoomPreferences(savedBooking, request.getPreferences());
        }
        
        // Update room status to RESERVED
        updateRoomStatuses(request.getRoomIds(), "RESERVED");
        
        log.info("Booking created successfully: {}", savedBooking.getBookingCode());
        return mapToResponse(savedBooking);
    }
    
    public BookingResponse getBookingById(Long bookingId) {
        log.debug("Fetching booking by ID: {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        return mapToResponse(booking);
    }
    
    public BookingResponse getBookingByCode(String bookingCode) {
        log.debug("Fetching booking by code: {}", bookingCode);
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with code: " + bookingCode));
        return mapToResponse(booking);
    }
    
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<BookingResponse> searchBookings(String keyword, Pageable pageable) {
        Page<Booking> bookingPage = bookingRepository.searchBookings(keyword, pageable);
        return bookingPage.map(this::mapToResponse);
    }
    
    public List<BookingResponse> getTodayCheckIns() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findTodayCheckIns(today).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getTodayCheckOuts() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findTodayCheckOuts(today).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getTodayArrivals() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findByCheckInDateAndStatusNot(today, BookingStatus.CHECKED_IN).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public BookingResponse processCheckIn(Long bookingId, Map<String, Object> checkinData) {
        log.info("Processing check-in for booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        // Validate booking can be checked in
        if (BookingStatus.CHECKED_IN.name().equals(booking.getStatus())) {
            throw new BusinessException("Booking is already checked in");
        }
        
        if (BookingStatus.CANCELLED.name().equals(booking.getStatus())) {
            throw new BusinessException("Cannot check in a cancelled booking");
        }
        
        // Update booking status and actual check-in time
        booking.setStatus(BookingStatus.CHECKED_IN.name());
        booking.setActualCheckIn(LocalDateTime.now());
        
        // Update payment status if deposit paid
        if (checkinData != null && Boolean.TRUE.equals(checkinData.get("depositPaid"))) {
            booking.setPaymentStatus("PARTIAL");
        }
        
        // Save booking
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Update room status to OCCUPIED
        updateRoomStatusesForBooking(booking, "OCCUPIED");
        
        log.info("Booking {} checked in at {}", bookingId, LocalDateTime.now());
        return mapToResponse(updatedBooking);
    }
    
    public BookingResponse processCheckOut(Long bookingId, CheckoutRequest checkoutRequest) {
        log.info("Processing check-out for booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        // Validate booking can be checked out
        if (!BookingStatus.CHECKED_IN.name().equals(booking.getStatus())) {
            throw new BusinessException("Booking is not checked in");
        }
        
        // Update booking status
        booking.setStatus(BookingStatus.CHECKED_OUT.name());
        booking.setActualCheckOut(LocalDateTime.now());
        
        // Apply discount if any
        if (checkoutRequest.getDiscountAmount() != null && 
            checkoutRequest.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = checkoutRequest.getDiscountAmount();
            if ("PERCENTAGE".equals(checkoutRequest.getDiscountType())) {
                BigDecimal currentTotal = calculateTotalAmount(booking);
                discount = currentTotal.multiply(discount)
                        .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
            }
            BigDecimal currentManualDiscount = booking.getManualDiscount() != null ? 
                                              booking.getManualDiscount() : BigDecimal.ZERO;
            booking.setManualDiscount(currentManualDiscount.add(discount));
        }
        
        // Update payment status based on balance
        BigDecimal balance = calculateBalance(booking);
        if (balance.compareTo(BigDecimal.ZERO) == 0) {
            booking.setPaymentStatus("PAID");
        } else {
            booking.setPaymentStatus("PARTIAL");
        }
        
        // Save booking
        Booking updatedBooking = bookingRepository.save(booking);
        
        // Update room status
        if (Boolean.TRUE.equals(checkoutRequest.getMarkRoomVacant())) {
            updateRoomStatusesForBooking(booking, "AVAILABLE");
        } else {
            updateRoomStatusesForBooking(booking, "CLEANING");
        }
        
        // Generate invoice if requested
        if (Boolean.TRUE.equals(checkoutRequest.getGenerateInvoice())) {
            try {
                invoiceService.generateInvoice(bookingId);
            } catch (Exception e) {
                log.error("Failed to generate invoice for booking {}: {}", bookingId, e.getMessage());
            }
        }
        
        log.info("Booking {} checked out at {}", bookingId, LocalDateTime.now());
        return mapToResponse(updatedBooking);
    }
    
    public BookingResponse cancelCheckIn(Long bookingId, String reason) {
        log.info("Cancelling check-in for booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        // Update booking status
        booking.setStatus(BookingStatus.CANCELLED.name());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        
        // Save booking
        Booking cancelledBooking = bookingRepository.save(booking);
        
        // Update room status back to AVAILABLE
        updateRoomStatusesForBooking(booking, "AVAILABLE");
        
        log.info("Check-in cancelled for booking: {}", bookingId);
        return mapToResponse(cancelledBooking);
    }
    
    public BookingResponse updateBookingStatus(Long bookingId, String newStatus) {
        log.info("Updating booking {} status to {}", bookingId, newStatus);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        String oldStatus = booking.getStatus();
        booking.setStatus(newStatus);
        
        // Handle specific status transitions
        if (BookingStatus.CHECKED_IN.name().equals(newStatus) && !BookingStatus.CHECKED_IN.name().equals(oldStatus)) {
            booking.setActualCheckIn(LocalDateTime.now());
            updateRoomStatusesForBooking(booking, "OCCUPIED");
        } else if (BookingStatus.CHECKED_OUT.name().equals(newStatus) && !BookingStatus.CHECKED_OUT.name().equals(oldStatus)) {
            booking.setActualCheckOut(LocalDateTime.now());
            updateRoomStatusesForBooking(booking, "CLEANING");
        } else if (BookingStatus.CANCELLED.name().equals(newStatus)) {
            booking.setCancelledAt(LocalDateTime.now());
            updateRoomStatusesForBooking(booking, "AVAILABLE");
        }
        
        Booking updatedBooking = bookingRepository.save(booking);
        
        log.info("Booking status updated: {}", bookingId);
        return mapToResponse(updatedBooking);
    }
    
    public void cancelBooking(Long bookingId, String reason) {
        log.info("Cancelling booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        booking.setStatus(BookingStatus.CANCELLED.name());
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancellationReason(reason);
        
        bookingRepository.save(booking);
        
        // Free up rooms
        updateRoomStatusesForBooking(booking, "AVAILABLE");
        
        log.info("Booking cancelled: {}", bookingId);
    }
    
    public List<BookingResponse> getExpectedArrivals(LocalDate date) {
        return bookingRepository.findByCheckInDate(date).stream()
                .filter(booking -> !BookingStatus.CHECKED_IN.name().equals(booking.getStatus()) &&
                                  !BookingStatus.CANCELLED.name().equals(booking.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getOccupiedBookingsForDate(LocalDate date) {
        return bookingRepository.findOccupiedRoomsByDate(date).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getTodayDepartures() {
        LocalDate today = LocalDate.now();
        return bookingRepository.findTodayCheckOuts(today).stream()
                .filter(booking -> !BookingStatus.CHECKED_OUT.name().equals(booking.getStatus()) &&
                                  !BookingStatus.CANCELLED.name().equals(booking.getStatus()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public BookingBillResponse getBookingBill(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + bookingId));
        
        // Get additional charges from service charges table
        List<ServiceCharge> serviceCharges = serviceChargeRepository.findByBookingBookingId(bookingId);
        
        List<BookingBillResponse.AdditionalCharge> additionalCharges = serviceCharges.stream()
                .map(sc -> BookingBillResponse.AdditionalCharge.builder()
                        .description(sc.getDescription())
                        .amount(sc.getAmount())
                        .category(sc.getCategory())
                        .quantity(sc.getQuantity())
                        .chargeDate(sc.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        // Get booking rooms
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        
        // Calculate totals
        BigDecimal roomCharges = calculateRoomCharges(booking);
        BigDecimal serviceChargesTotal = calculateServiceChargesTotal(serviceCharges);
        BigDecimal subtotal = roomCharges.add(serviceChargesTotal);
        
        BigDecimal discount = calculateDiscount(booking);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        
        BigDecimal tax = afterDiscount.multiply(booking.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal total = afterDiscount.add(tax);
        BigDecimal paid = getAmountPaid(bookingId);
        BigDecimal balance = total.subtract(paid);
        
        // Get room info
        String roomNumber = "N/A";
        String roomType = "N/A";
        if (!bookingRooms.isEmpty()) {
            Room room = bookingRooms.get(0).getRoom();
            roomNumber = room.getRoomNumber();
            roomType = room.getRoomType(); // FIXED: Changed from getType() to getRoomType()
        }
        
        return BookingBillResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .guest(guestService.getGuestById(booking.getGuest().getGuestId()))
                .roomNumber(roomNumber)
                .roomType(roomType)
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .nights(booking.getNights())
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .roomCharges(roomCharges)
                .serviceCharges(serviceChargesTotal)
                .additionalCharges(additionalCharges)
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(tax)
                .totalAmount(total)
                .amountPaid(paid)
                .balanceDue(balance)
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .checkedOutAt(booking.getActualCheckOut())
                .build();
    }
    
    
    // Helper methods
    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new BusinessException("Check-in date must be before check-out date");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new BusinessException("Check-in date cannot be in the past");
        }
    }
    
    private Guest getOrCreateGuest(BookingRequest request) {
        if (request.getGuestId() != null) {
            return guestService.getGuestEntityById(request.getGuestId());
        } else if (request.getGuestDetails() != null) {
            GuestResponse guestResponse = guestService.createGuest(request.getGuestDetails());
            return guestService.getGuestEntityById(guestResponse.getGuestId());
        } else {
            throw new BusinessException("Guest information is required");
        }
    }
    
    private void validateRoomAvailability(List<Long> roomIds, LocalDate checkIn, LocalDate checkOut) {
        for (Long roomId : roomIds) {
            List<Room> availableRooms = roomRepository.findAvailableRooms(checkIn, checkOut, null);
            boolean isAvailable = availableRooms.stream()
                    .anyMatch(room -> room.getRoomId().equals(roomId));
            
            if (!isAvailable) {
                Room room = roomService.getRoomEntityById(roomId);
                throw new BusinessException("Room " + room.getRoomNumber() + " is not available for selected dates");
            }
        }
    }
    
    private void assignRoomsToBooking(Booking booking, List<Long> roomIds) {
        for (Long roomId : roomIds) {
            Room room = roomService.getRoomEntityById(roomId);
            
            BookingRoom bookingRoom = new BookingRoom();
            bookingRoom.setBooking(booking);
            bookingRoom.setRoom(room);
            bookingRoom.setRoomRate(room.getBaseRate());
            bookingRoom.setGuest(booking.getGuest()); // Assign primary guest initially
            
            bookingRoomRepository.save(bookingRoom);
        }
    }
    
    private void addGroupMembers(Booking booking, List<GuestRequest> groupMemberRequests) {
        for (int i = 0; i < groupMemberRequests.size(); i++) {
            GuestRequest memberRequest = groupMemberRequests.get(i);
            GuestResponse memberResponse = guestService.createGuest(memberRequest);
            Guest memberGuest = guestService.getGuestEntityById(memberResponse.getGuestId());
            
            GroupMember groupMember = new GroupMember();
            groupMember.setBooking(booking);
            groupMember.setGuest(memberGuest);
            groupMember.setIsLeader(i == 0); // First member is leader
            
            groupMemberRepository.save(groupMember);
        }
    }
    
    private void addRoomPreferences(Booking booking, com.hotel.reception.model.dto.request.RoomPreferenceRequest prefRequest) {
        RoomPreference preference = new RoomPreference();
        preference.setBooking(booking);
        preference.setBedPreference(prefRequest.getBedPreference());
        preference.setSmokingPreference(prefRequest.getSmokingPreference());
        preference.setFloorPreference(prefRequest.getFloorPreference());
        preference.setViewPreference(prefRequest.getViewPreference());
        preference.setExtraBed(prefRequest.getExtraBed());
        preference.setCrib(prefRequest.getCrib());
        preference.setWheelchairAccess(prefRequest.getWheelchairAccess());
        preference.setEarlyCheckIn(prefRequest.getEarlyCheckIn());
        preference.setLateCheckOut(prefRequest.getLateCheckOut());
        
        roomPreferenceRepository.save(preference);
    }
    
    private void updateRoomStatuses(List<Long> roomIds, String status) {
        for (Long roomId : roomIds) {
            roomService.updateRoomStatus(roomId, status);
        }
    }
    
    private void updateRoomStatusesForBooking(Booking booking, String status) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        for (BookingRoom bookingRoom : bookingRooms) {
            roomService.updateRoomStatus(bookingRoom.getRoom().getRoomId(), status);
        }
    }
    
    // Financial calculation helper methods
    private BigDecimal calculateRoomCharges(Booking booking) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        return bookingRooms.stream()
                .map(br -> safe(br.getRoomRate()).multiply(BigDecimal.valueOf(booking.getNights())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateServiceChargesTotal(List<ServiceCharge> serviceCharges) {
        return serviceCharges.stream()
                .map(ServiceCharge::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateTotalAmount(Booking booking) {
        BigDecimal roomCharges = calculateRoomCharges(booking);
        List<ServiceCharge> serviceCharges = serviceChargeRepository.findByBookingBookingId(booking.getBookingId());
        BigDecimal serviceChargesTotal = calculateServiceChargesTotal(serviceCharges);
        BigDecimal subtotal = roomCharges.add(serviceChargesTotal);
        
        BigDecimal discount = calculateDiscount(booking);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        
        return afterDiscount.add(afterDiscount.multiply(booking.getTaxPercentage())
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP));
    }
    
    private BigDecimal calculateDiscount(Booking booking) {
        BigDecimal roomCharges = calculateRoomCharges(booking);
        List<ServiceCharge> serviceCharges = serviceChargeRepository.findByBookingBookingId(booking.getBookingId());
        BigDecimal serviceChargesTotal = calculateServiceChargesTotal(serviceCharges);
        BigDecimal subtotal = roomCharges.add(serviceChargesTotal);
        
        BigDecimal percentageDiscount = subtotal
                .multiply(safe(booking.getDiscountPercentage()))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);
        
        return percentageDiscount.add(safe(booking.getManualDiscount()));
    }
    
    private BigDecimal calculateBalance(Booking booking) {
        BigDecimal total = calculateTotalAmount(booking);
        BigDecimal paid = getAmountPaid(booking.getBookingId());
        return total.subtract(paid);
    }
    
    private BigDecimal getAmountPaid(Long bookingId) {
        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
        return payments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .map(Payment::getAmountPaid)  // Changed from getAmount() to getAmountPaid()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
//    private BigDecimal getAmountPaid(Long bookingId) {
//        List<Payment> payments = paymentRepository.findByBookingBookingId(bookingId);
//        return payments.stream()
//                .filter(p -> "COMPLETED".equals(p.getStatus()))
//                .map(Payment::getAmount)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
//    
    
    private BookingResponse mapToResponse(Booking booking) {
        // Guest
        GuestResponse guestResponse = guestService.getGuestById(booking.getGuest().getGuestId());

        // Rooms
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        List<RoomResponse> roomResponses = bookingRooms.stream()
                .map(br -> roomService.getRoomById(br.getRoom().getRoomId()))
                .collect(Collectors.toList());

        // Group members
        List<GroupMember> groupMembers = groupMemberRepository.findByBookingBookingId(booking.getBookingId());
        List<GuestResponse> groupMemberResponses = groupMembers.stream()
                .map(gm -> guestService.getGuestById(gm.getGuest().getGuestId()))
                .collect(Collectors.toList());

        // Room preferences (if any)
        Optional<RoomPreference> roomPreferenceOpt = roomPreferenceRepository.findByBookingBookingId(booking.getBookingId());
        RoomPreferenceResponse preferenceResponse = null;
        if (roomPreferenceOpt.isPresent()) {
            RoomPreference pref = roomPreferenceOpt.get();
            preferenceResponse = RoomPreferenceResponse.builder()
                    .bedPreference(pref.getBedPreference())
                    .smokingPreference(pref.getSmokingPreference())
                    .floorPreference(pref.getFloorPreference())
                    .viewPreference(pref.getViewPreference())
                    .extraBed(pref.getExtraBed())
                    .crib(pref.getCrib())
                    .wheelchairAccess(pref.getWheelchairAccess())
                    .earlyCheckIn(pref.getEarlyCheckIn())
                    .lateCheckOut(pref.getLateCheckOut())
                    .build();
        }

        // Get service charges for this booking
        List<ServiceCharge> serviceCharges = serviceChargeRepository.findByBookingBookingId(booking.getBookingId());
        BigDecimal serviceChargesTotal = calculateServiceChargesTotal(serviceCharges);
        
        // Financial calculations
        int nights = booking.getNights() != null ? booking.getNights() : 1;

        BigDecimal roomCharges = calculateRoomCharges(booking);
        BigDecimal discount = calculateDiscount(booking);
        BigDecimal subtotal = roomCharges.add(serviceChargesTotal);
        BigDecimal afterDiscount = subtotal.subtract(discount);

        BigDecimal taxAmount = afterDiscount
                .multiply(safe(booking.getTaxPercentage()))
                .divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP);

        BigDecimal totalAmount = afterDiscount.add(taxAmount);
        BigDecimal amountPaid = getAmountPaid(booking.getBookingId());
        BigDecimal balanceDue = totalAmount.subtract(amountPaid);

        // Build response
        return BookingResponse.builder()
                .bookingId(booking.getBookingId())
                .bookingCode(booking.getBookingCode())
                .guest(guestResponse)
                .groupMembers(groupMemberResponses)
                .roomPreferences(preferenceResponse)
                .bookingType(booking.getBookingType())
                .bookingSource(booking.getBookingSource())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate())
                .checkInTime(booking.getCheckInTime())
                .checkOutTime(booking.getCheckOutTime())
                .actualCheckIn(booking.getActualCheckIn())
                .actualCheckOut(booking.getActualCheckOut())
                .nights(nights)
                .adults(booking.getAdults())
                .children(booking.getChildren())
                .infants(booking.getInfants())
                .rooms(roomResponses)
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .roomCharges(roomCharges)
                .serviceCharges(serviceChargesTotal)
                .subtotal(subtotal)
                .discountAmount(discount)
                .taxAmount(taxAmount)
                .totalAmount(totalAmount)
                .amountPaid(amountPaid)
                .balanceDue(balanceDue)
                .purposeOfVisit(booking.getPurposeOfVisit())
                .specialInstructions(booking.getSpecialInstructions())
                .cancellationReason(booking.getCancellationReason())
                .createdAt(booking.getCreatedAt())
                .createdBy(booking.getCreatedBy())
                .build();
    }
    
    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
    
    private String getRoomType(Booking booking) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        if (!bookingRooms.isEmpty()) {
            return bookingRooms.get(0).getRoom().getRoomType(); // Changed from getType() to getRoomType()
        }
        return "N/A";
    }
    
//    public Page<BookingResponse> searchDepartures(String keyword, String roomType, String status, 
//            String paymentMethod, Pageable pageable) {
//log.info("Searching departures with keyword: {}, roomType: {}, status: {}, paymentMethod: {}", 
//keyword, roomType, status, paymentMethod);
//
//// Build specification for filtering
//Specification<Booking> spec = Specification.where(null);
//
//// Filter by check-out date (today or in the past for departures)
//LocalDate today = LocalDate.now();
//spec = spec.and((root, query, cb) -> 
//cb.lessThanOrEqualTo(root.get("checkOutDate"), today));
//
//// Exclude cancelled bookings
//spec = spec.and((root, query, cb) -> 
//cb.notEqual(root.get("status"), BookingStatus.CANCELLED.name()));
//
//// Keyword search
//if (StringUtils.hasText(keyword)) {
//spec = spec.and((root, query, cb) -> cb.or(
//cb.like(cb.lower(root.get("guest").get("firstName")), "%" + keyword.toLowerCase() + "%"),
//cb.like(cb.lower(root.get("guest").get("lastName")), "%" + keyword.toLowerCase() + "%"),
//cb.like(root.get("bookingCode"), "%" + keyword + "%"),
//cb.like(root.get("guest").get("email"), "%" + keyword + "%"),
//cb.like(root.get("guest").get("phoneNumber"), "%" + keyword + "%")
//));
//}
//
//// Room type filter
//if (StringUtils.hasText(roomType) && !"all".equalsIgnoreCase(roomType)) {
//spec = spec.and((root, query, cb) -> {
//Join<Booking, BookingRoom> bookingRooms = root.join("bookingRooms", JoinType.LEFT);
//Join<BookingRoom, Room> room = bookingRooms.join("room", JoinType.LEFT);
//return cb.equal(room.get("roomType"), roomType);
//});
//}
//
//// Status filter
//if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
//spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
//}
//
//// Payment method filter (this is more complex as it requires joining with payments)
//if (StringUtils.hasText(paymentMethod) && !"all".equalsIgnoreCase(paymentMethod)) {
//// We'll filter by payment status instead, as payment method might be in Payment entity
//// For simplicity, filter by payment status
//if (paymentMethod.equalsIgnoreCase("paid")) {
//spec = spec.and((root, query, cb) -> 
//cb.equal(root.get("paymentStatus"), "PAID"));
//} else if (paymentMethod.equalsIgnoreCase("pending")) {
//spec = spec.and((root, query, cb) -> 
//cb.equal(root.get("paymentStatus"), "PENDING"));
//} else if (paymentMethod.equalsIgnoreCase("partial")) {
//spec = spec.and((root, query, cb) -> 
//cb.equal(root.get("paymentStatus"), "PARTIAL"));
//}
//}
//
//// Execute query
//Page<Booking> bookings = bookingRepository.findAll(spec, pageable);
//return bookings.map(this::mapToResponse);
//}
    
    
    
    
    public Page<BookingResponse> searchDepartures(String keyword, String roomType, String status, 
            String paymentMethod, Pageable pageable) {
log.info("Searching departures with keyword: {}, roomType: {}, status: {}, paymentMethod: {}", 
keyword, roomType, status, paymentMethod);

// Get all bookings that are checked out or have check-out date today or in the past
LocalDate today = LocalDate.now();
List<Booking> allBookings = bookingRepository.findAll();

// Filter manually (for now - you can optimize with a custom query later)
List<BookingResponse> filteredResponses = allBookings.stream()
.filter(booking -> {
// Check if booking is for departure (check-out date is today or in the past)
if (booking.getCheckOutDate() == null) return false;
return !booking.getCheckOutDate().isAfter(today);
})
.filter(booking -> {
// Exclude cancelled bookings
return !BookingStatus.CANCELLED.name().equals(booking.getStatus());
})
.filter(booking -> {
// Keyword filter
if (StringUtils.hasText(keyword)) {
String guestName = (booking.getGuest().getFirstName() + " " + booking.getGuest().getLastName()).toLowerCase();
String bookingCode = booking.getBookingCode().toLowerCase();
String guestEmail = booking.getGuest().getEmail() != null ? booking.getGuest().getEmail().toLowerCase() : "";
String guestPhone = booking.getGuest().getPhone() != null ? booking.getGuest().getPhone() : "";

return guestName.contains(keyword.toLowerCase()) ||
bookingCode.contains(keyword.toLowerCase()) ||
guestEmail.contains(keyword.toLowerCase()) ||
guestPhone.contains(keyword);
}
return true;
})
.filter(booking -> {
// Room type filter
if (StringUtils.hasText(roomType) && !"all".equalsIgnoreCase(roomType)) {
List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
return bookingRooms.stream()
.anyMatch(br -> roomType.equalsIgnoreCase(br.getRoom().getRoomType()));
}
return true;
})
.filter(booking -> {
// Status filter
if (StringUtils.hasText(status) && !"all".equalsIgnoreCase(status)) {
return status.equalsIgnoreCase(booking.getStatus());
}
return true;
})
.filter(booking -> {
// Payment method filter
if (StringUtils.hasText(paymentMethod) && !"all".equalsIgnoreCase(paymentMethod)) {
// Get payments for this booking
List<Payment> payments = paymentRepository.findByBookingBookingId(booking.getBookingId());
if (!payments.isEmpty()) {
return payments.stream()
.anyMatch(p -> paymentMethod.equalsIgnoreCase(p.getPaymentMethod()));
}
return false;
}
return true;
})
.map(this::mapToResponse)
.collect(Collectors.toList());

// Apply pagination manually
int start = (int) pageable.getOffset();
int end = Math.min((start + pageable.getPageSize()), filteredResponses.size());

return new PageImpl<>(
filteredResponses.subList(start, end), 
pageable, 
filteredResponses.size()
);
}
    
    
}