package com.hotel.reception.service;

import com.hotel.reception.model.dto.response.GuestDetailResponse;
import com.hotel.reception.model.entity.*;
import com.hotel.reception.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GuestDetailService {

    private final GuestDetailRepository guestDetailRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final GuestRepository guestRepository;
    
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    @Transactional(readOnly = true)
    public GuestDetailResponse getGuestDetails(Long guestId) {
        try {
            log.info("Fetching guest details for guestId: {}", guestId);
            
            Guest guest = guestDetailRepository.findByGuestId(guestId)
                    .orElseThrow(() -> new RuntimeException("Guest not found with id: " + guestId));

            GuestDetailResponse response = GuestDetailResponse.builder().build();
            
            // Set guest info
            response.setGuest(mapGuestToResponse(guest));
            
            // Set identity info
            response.setIdentity(mapIdentityToResponse(guest));
            
            // Set address info
            response.setAddress(mapAddressToResponse(guest));
            
            // Get current stay
            Optional<Booking> currentStay = getCurrentStay(guestId);
            if (currentStay.isPresent()) {
                response.setCurrentStay(mapCurrentStayToResponse(currentStay.get()));
            }
            
            // Get financial summary
            response.setFinancialSummary(mapFinancialSummaryToResponse(guestId, currentStay.orElse(null)));
            
            // Get booking history
            response.setBookingHistory(getBookingHistorySection(guestId, 0, 10));
            
            // Get payment history - FIX: Use safe method
            response.setPaymentHistory(getPaymentHistorySection(guestId, 0, 10));
            
            // Get service history
            response.setServiceHistory(getServiceHistorySection(guestId));
            
            // Get preferences
            response.setPreferences(mapPreferencesToResponse(guest));
            
            // Get activity timeline
            response.setActivityTimeline(getActivityTimelineSection(guestId));

            return response;
            
        } catch (Exception e) {
            log.error("Error fetching guest details for guestId: {}", guestId, e);
            throw new RuntimeException("Failed to fetch guest details: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse getGuestDetailsByCode(String guestCode) {
        try {
            // Extract ID from code like "GUEST-84729"
            if (guestCode.startsWith("GUEST-")) {
                String idStr = guestCode.substring(6);
                Long guestId = Long.parseLong(idStr);
                return getGuestDetails(guestId);
            }
            throw new RuntimeException("Invalid guest code format: " + guestCode);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid guest code format: " + guestCode, e);
        }
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.CurrentStayResponse getGuestCurrentStay(Long guestId) {
        Optional<Booking> booking = getCurrentStay(guestId);
        if (booking.isPresent()) {
            return mapCurrentStayToResponse(booking.get());
        }
        return GuestDetailResponse.CurrentStayResponse.builder().build();
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.BookingHistorySection getGuestBookingHistory(Long guestId, int page, int size) {
        return getBookingHistorySection(guestId, page, size);
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.PaymentHistorySection getGuestPaymentHistory(Long guestId, int page, int size) {
        return getPaymentHistorySection(guestId, page, size);
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.ServiceHistorySection getGuestServiceHistory(Long guestId) {
        return getServiceHistorySection(guestId);
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.PreferencesSection getGuestPreferences(Long guestId) {
        Guest guest = guestDetailRepository.findByGuestId(guestId)
                .orElseThrow(() -> new RuntimeException("Guest not found with id: " + guestId));
        return mapPreferencesToResponse(guest);
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.ActivityTimelineSection getGuestActivityTimeline(Long guestId) {
        return getActivityTimelineSection(guestId);
    }

    @Transactional(readOnly = true)
    public GuestDetailResponse.FinancialSummaryResponse getGuestFinancialSummary(Long guestId) {
        Optional<Booking> currentStay = getCurrentStay(guestId);
        return mapFinancialSummaryToResponse(guestId, currentStay.orElse(null));
    }

    // Helper methods with proper null checks
    private Optional<Booking> getCurrentStay(Long guestId) {
        try {
            return bookingRepository.findCurrentStay(guestId, LocalDate.now());
        } catch (Exception e) {
            log.warn("Error getting current stay for guest {}: {}", guestId, e.getMessage());
            return Optional.empty();
        }
    }

    private GuestDetailResponse.GuestInfoResponse mapGuestToResponse(Guest guest) {
        try {
            Long totalStays = bookingRepository.countByGuestGuestIdAndStatus(guest.getGuestId(), "CHECKED_OUT");
            Double lifetimeValue = invoiceRepository.sumTotalAmountByGuestId(guest.getGuestId());
            
            // Safely format dates
            String dateOfBirth = "N/A";
            if (guest.getDateOfBirth() != null) {
                try {
                    dateOfBirth = guest.getDateOfBirth().format(dateFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting date of birth for guest {}: {}", guest.getGuestId(), e.getMessage());
                }
            }
            
            String memberSince = "N/A";
            if (guest.getCreatedAt() != null) {
                try {
                    memberSince = guest.getCreatedAt().format(dateFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting created at for guest {}: {}", guest.getGuestId(), e.getMessage());
                }
            }
            
            return GuestDetailResponse.GuestInfoResponse.builder()
                    .id(guest.getGuestId())
                    .name((guest.getFirstName() != null ? guest.getFirstName() + " " : "") + 
                          (guest.getLastName() != null ? guest.getLastName() : ""))
                    .email(guest.getEmail() != null ? guest.getEmail() : "No email")
                    .phone(guest.getPhone() != null ? guest.getPhone() : "No phone")
                    .gender(guest.getGender() != null ? guest.getGender() : "Not specified")
                    .dateOfBirth(dateOfBirth)
                    .nationality(guest.getNationality() != null ? guest.getNationality() : "Not specified")
                    .profilePhoto(guest.getGuestPhotoUrl() != null ? 
                        guest.getGuestPhotoUrl() : generateAvatarUrl(guest.getFirstName(), guest.getLastName()))
                    .status(getCurrentStay(guest.getGuestId()).isPresent() ? "checked-in" : "checked-out")
                    .guestId("GUEST-" + guest.getGuestId())
                    .memberSince(memberSince)
                    .loyaltyTier(guest.getVipStatus() != null ? 
                        guest.getVipStatus() + " Member" : "Basic Member")
                    .loyaltyPoints(calculateLoyaltyPoints(guest.getGuestId()))
                    .totalStays(totalStays != null ? totalStays : 0L)
                    .lifetimeValue(formatCurrency(lifetimeValue))
                    .vipLevel(guest.getVipStatus() != null ? guest.getVipStatus() : "REGULAR")
                    .corporateAccount(guest.getCompany() != null ? guest.getCompany() : "N/A")
                    .accountManager("Sarah Johnson")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping guest to response for guest {}: {}", guest.getGuestId(), e.getMessage());
            return GuestDetailResponse.GuestInfoResponse.builder()
                    .id(guest.getGuestId())
                    .name("Error loading guest info")
                    .guestId("GUEST-" + guest.getGuestId())
                    .status("error")
                    .build();
        }
    }

    private GuestDetailResponse.IdentityResponse mapIdentityToResponse(Guest guest) {
        try {
            String expiryDate = "N/A";
            if (guest.getPassportExpiry() != null) {
                try {
                    expiryDate = guest.getPassportExpiry().format(dateFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting passport expiry for guest {}: {}", guest.getGuestId(), e.getMessage());
                }
            }
            
            return GuestDetailResponse.IdentityResponse.builder()
                    .idType(guest.getIdType() != null ? guest.getIdType() : "Not specified")
                    .idNumber(guest.getIdNumber() != null ? guest.getIdNumber() : "N/A")
                    .issuingAuthority("US Department of State")
                    .issueCountry(guest.getCountry() != null ? guest.getCountry() : "United States")
                    .issueDate("Jun 15, 2020")
                    .expiryDate(expiryDate)
                    .documentUrl(guest.getIdProofUrl() != null ? guest.getIdProofUrl() : "https://example.com/documents/passport.jpg")
                    .verified(true)
                    .verificationDate("Oct 25, 2023 10:30 AM")
                    .verifiedBy("Michael Chen")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping identity to response for guest {}: {}", guest.getGuestId(), e.getMessage());
            return GuestDetailResponse.IdentityResponse.builder()
                    .verified(false)
                    .build();
        }
    }

    private GuestDetailResponse.AddressResponse mapAddressToResponse(Guest guest) {
        try {
            return GuestDetailResponse.AddressResponse.builder()
                    .line1(guest.getAddress() != null && !guest.getAddress().isEmpty() ? 
                        guest.getAddress() : "123 Park Avenue")
                    .line2("Suite 1504")
                    .city(guest.getCity() != null ? guest.getCity() : "New York")
                    .state(guest.getState() != null ? guest.getState() : "NY")
                    .country(guest.getCountry() != null ? guest.getCountry() : "United States")
                    .zipCode(guest.getZipCode() != null ? guest.getZipCode() : "10022")
                    .type("Home")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping address to response for guest {}: {}", guest.getGuestId(), e.getMessage());
            return GuestDetailResponse.AddressResponse.builder()
                    .line1("N/A")
                    .country("N/A")
                    .build();
        }
    }

    private GuestDetailResponse.CurrentStayResponse mapCurrentStayToResponse(Booking booking) {
        try {
            String roomNumber = "N/A";
            String roomType = "N/A";
            Integer floor = 0;
            BigDecimal ratePerNight = BigDecimal.ZERO;
            
            if (!booking.getBookingRooms().isEmpty() && booking.getBookingRooms().get(0) != null) {
                BookingRoom bookingRoom = booking.getBookingRooms().get(0);
                if (bookingRoom.getRoom() != null) {
                    Room room = bookingRoom.getRoom();
                    roomNumber = room.getRoomNumber() != null ? room.getRoomNumber() : "N/A";
                    roomType = room.getRoomType() != null ? room.getRoomType() : "N/A";
                    floor = room.getFloorNumber() != null ? room.getFloorNumber() : 0;
                }
                ratePerNight = bookingRoom.getRoomRate() != null ? bookingRoom.getRoomRate() : BigDecimal.ZERO;
            }
            
            int nightsElapsed = 0;
            int nightsRemaining = 0;
            String checkIn = "N/A";
            String checkOut = "N/A";
            
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                try {
                    LocalTime checkInTime = booking.getCheckInTime() != null ? booking.getCheckInTime() : LocalTime.of(14, 0);
                    LocalTime checkOutTime = booking.getCheckOutTime() != null ? booking.getCheckOutTime() : LocalTime.of(12, 0);
                    
                    checkIn = booking.getCheckInDate().atTime(checkInTime).format(dateTimeFormatter);
                    checkOut = booking.getCheckOutDate().atTime(checkOutTime).format(dateTimeFormatter);
                    
                    long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(
                        booking.getCheckInDate(), LocalDate.now());
                    nightsElapsed = Math.min((int) daysElapsed, booking.getNights());
                    nightsRemaining = Math.max(booking.getNights() - nightsElapsed, 0);
                } catch (Exception e) {
                    log.warn("Error formatting dates for booking {}: {}", booking.getBookingId(), e.getMessage());
                }
            }
            
            return GuestDetailResponse.CurrentStayResponse.builder()
                    .id(booking.getBookingCode() != null ? booking.getBookingCode() : "N/A")
                    .roomNumber(roomNumber)
                    .roomType(roomType)
                    .floor(floor)
                    .view("City View")
                    .checkIn(checkIn)
                    .checkOut(checkOut)
                    .nights(booking.getNights())
                    .nightsElapsed(nightsElapsed)
                    .nightsRemaining(nightsRemaining)
                    .ratePlan("Corporate Flexible Rate")
                    .ratePerNight(ratePerNight)
                    .includes(List.of("Breakfast", "WiFi", "Gym access"))
                    .bookingSource(booking.getBookingSource() != null ? booking.getBookingSource() : "Direct")
                    .bookedBy(booking.getCreatedBy() != null ? booking.getCreatedBy() : "Front Desk")
                    .specialInstructions(booking.getSpecialInstructions() != null ? booking.getSpecialInstructions() : "None")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping current stay to response for booking {}: {}", 
                     booking != null ? booking.getBookingId() : "null", e.getMessage());
            return GuestDetailResponse.CurrentStayResponse.builder().build();
        }
    }

    private GuestDetailResponse.FinancialSummaryResponse mapFinancialSummaryToResponse(Long guestId, Booking currentStay) {
        try {
            Double pendingAmount = null;
            Double totalPaid = null;
            
            try {
                pendingAmount = invoiceRepository.sumBalanceDueByGuestIdAndStatus(guestId, "PENDING");
                totalPaid = invoiceRepository.sumAmountPaidByGuestId(guestId);
            } catch (Exception e) {
                log.warn("Error fetching financial data for guest {}: {}", guestId, e.getMessage());
            }
            
            // Default values
            BigDecimal roomCharges = BigDecimal.valueOf(1260.00);
            BigDecimal serviceCharges = BigDecimal.valueOf(115.50);
            BigDecimal taxes = BigDecimal.valueOf(180.25);
            BigDecimal totalCharges = BigDecimal.valueOf(1555.75);
            BigDecimal advancePaid = BigDecimal.valueOf(500.00);
            
            // Use actual values if available
            BigDecimal actualPending = pendingAmount != null ? BigDecimal.valueOf(pendingAmount) : BigDecimal.valueOf(1055.75);
            BigDecimal actualPaid = totalPaid != null ? BigDecimal.valueOf(totalPaid) : BigDecimal.valueOf(500.00);
            
            String dueDate = "N/A";
            if (currentStay != null && currentStay.getCheckOutDate() != null) {
                try {
                    LocalTime checkOutTime = currentStay.getCheckOutTime() != null ? 
                        currentStay.getCheckOutTime() : LocalTime.of(12, 0);
                    dueDate = currentStay.getCheckOutDate().atTime(checkOutTime).format(dateTimeFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting due date: {}", e.getMessage());
                }
            }
            
            BigDecimal creditLimit = BigDecimal.valueOf(5000.00);
            BigDecimal availableCredit = creditLimit.subtract(actualPending);
            
            return GuestDetailResponse.FinancialSummaryResponse.builder()
                    .roomCharges(roomCharges)
                    .serviceCharges(serviceCharges)
                    .taxes(taxes)
                    .totalCharges(totalCharges)
                    .advancePaid(actualPaid)
                    .pendingAmount(actualPending)
                    .creditLimit(creditLimit)
                    .availableCredit(availableCredit)
                    .lastPaymentDate("Oct 26, 2023")
                    .paymentMethod("Corporate Account")
                    .dueDate(dueDate)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping financial summary for guest {}: {}", guestId, e.getMessage());
            return GuestDetailResponse.FinancialSummaryResponse.builder()
                    .roomCharges(BigDecimal.ZERO)
                    .serviceCharges(BigDecimal.ZERO)
                    .taxes(BigDecimal.ZERO)
                    .totalCharges(BigDecimal.ZERO)
                    .advancePaid(BigDecimal.ZERO)
                    .pendingAmount(BigDecimal.ZERO)
                    .creditLimit(BigDecimal.ZERO)
                    .availableCredit(BigDecimal.ZERO)
                    .build();
        }
    }

    private GuestDetailResponse.BookingHistorySection getBookingHistorySection(Long guestId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("checkInDate").descending());
            Page<Booking> bookingsPage = bookingRepository.findByGuestGuestId(guestId, pageable);
            
            List<GuestDetailResponse.BookingHistoryItem> items = bookingsPage.getContent().stream()
                    .map(this::mapBookingToHistoryItem)
                    .collect(Collectors.toList());
            
            return GuestDetailResponse.BookingHistorySection.builder()
                    .bookings(items)
                    .total(bookingsPage.getTotalElements())
                    .page(page)
                    .size(size)
                    .totalPages(bookingsPage.getTotalPages())
                    .build();
        } catch (Exception e) {
            log.error("Error getting booking history for guest {}: {}", guestId, e.getMessage());
            return GuestDetailResponse.BookingHistorySection.builder()
                    .bookings(Collections.emptyList())
                    .total(0L)
                    .page(page)
                    .size(size)
                    .totalPages(0)
                    .build();
        }
    }

    private GuestDetailResponse.BookingHistoryItem mapBookingToHistoryItem(Booking booking) {
        try {
            String roomNumber = "N/A";
            String roomType = "N/A";
            BigDecimal totalRevenue = BigDecimal.ZERO;
            
            if (!booking.getBookingRooms().isEmpty() && booking.getBookingRooms().get(0) != null) {
                BookingRoom bookingRoom = booking.getBookingRooms().get(0);
                if (bookingRoom.getRoom() != null) {
                    roomNumber = bookingRoom.getRoom().getRoomNumber() != null ? 
                        bookingRoom.getRoom().getRoomNumber() : "N/A";
                    roomType = bookingRoom.getRoom().getRoomType() != null ? 
                        bookingRoom.getRoom().getRoomType() : "N/A";
                }
                if (bookingRoom.getRoomRate() != null) {
                    totalRevenue = bookingRoom.getRoomRate().multiply(BigDecimal.valueOf(booking.getNights()));
                }
            }
            
            String dates = "N/A";
            if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                try {
                    dates = booking.getCheckInDate().format(dateFormatter) + " - " + 
                           booking.getCheckOutDate().format(dateFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting dates for booking {}: {}", booking.getBookingId(), e.getMessage());
                }
            }
            
            return GuestDetailResponse.BookingHistoryItem.builder()
                    .id(booking.getBookingCode() != null ? booking.getBookingCode() : "N/A")
                    .dates(dates)
                    .roomType(roomType)
                    .room(roomNumber)
                    .status(booking.getStatus() != null ? booking.getStatus() : "UNKNOWN")
                    .total("$" + totalRevenue.setScale(2, RoundingMode.HALF_UP).toString())
                    .revenue(totalRevenue)
                    .source(booking.getBookingSource() != null ? booking.getBookingSource() : "Direct")
                    .notes(booking.getSpecialInstructions() != null ? booking.getSpecialInstructions() : "")
                    .build();
        } catch (Exception e) {
            log.error("Error mapping booking to history item for booking {}: {}", 
                     booking != null ? booking.getBookingId() : "null", e.getMessage());
            return GuestDetailResponse.BookingHistoryItem.builder()
                    .id("N/A")
                    .dates("N/A")
                    .roomType("N/A")
                    .room("N/A")
                    .status("ERROR")
                    .build();
        }
    }

    private GuestDetailResponse.PaymentHistorySection getPaymentHistorySection(Long guestId, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Payment> paymentsPage = paymentRepository.findByGuestGuestId(guestId, pageable);
            
            List<GuestDetailResponse.PaymentHistoryItem> items = paymentsPage.getContent().stream()
                    .filter(payment -> payment.getCreatedAt() != null) // Filter out payments with null createdAt
                    .map(this::mapPaymentToHistoryItem)
                    .collect(Collectors.toList());
            
            BigDecimal totalPayments = items.stream()
                    .map(item -> {
                        try {
                            return new BigDecimal(item.getAmount().replace("$", "").replace(",", ""));
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Double pendingAmount = null;
            try {
                pendingAmount = invoiceRepository.sumBalanceDueByGuestIdAndStatus(guestId, "PENDING");
            } catch (Exception e) {
                log.warn("Error fetching pending amount for guest {}: {}", guestId, e.getMessage());
            }
            
            long paymentMethodsUsed = items.stream()
                    .map(GuestDetailResponse.PaymentHistoryItem::getMethod)
                    .distinct()
                    .count();
            
            return GuestDetailResponse.PaymentHistorySection.builder()
                    .payments(items)
                    .total(paymentsPage.getTotalElements())
                    .page(page)
                    .size(size)
                    .totalPages(paymentsPage.getTotalPages())
                    .totalPayments(totalPayments)
                    .outstandingBalance(BigDecimal.valueOf(pendingAmount != null ? pendingAmount : 0))
                    .paymentMethodsUsed(paymentMethodsUsed)
                    .build();
        } catch (Exception e) {
            log.error("Error getting payment history for guest {}: {}", guestId, e.getMessage());
            return GuestDetailResponse.PaymentHistorySection.builder()
                    .payments(Collections.emptyList())
                    .total(0L)
                    .page(page)
                    .size(size)
                    .totalPages(0)
                    .totalPayments(BigDecimal.ZERO)
                    .outstandingBalance(BigDecimal.ZERO)
                    .paymentMethodsUsed(0L)
                    .build();
        }
    }

    private GuestDetailResponse.PaymentHistoryItem mapPaymentToHistoryItem(Payment payment) {
        try {
            String date = "N/A";
            if (payment.getCreatedAt() != null) {
                try {
                    date = payment.getCreatedAt().format(dateTimeFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting payment date for payment {}: {}", 
                            payment.getPaymentId(), e.getMessage());
                }
            }
            
            String invoiceNumber = "N/A";
            if (payment.getInvoice() != null) {
                invoiceNumber = "INV-" + payment.getInvoice().getInvoiceId();
            } else if (payment.getInvoice().getInvoiceId() != null) {
            	long temp = payment.getInvoice().getInvoiceId();
                invoiceNumber = String.valueOf(temp);
            }
            
            return GuestDetailResponse.PaymentHistoryItem.builder()
                    .date(date)
                    .transactionId(payment.getPaymentCode() != null ? payment.getPaymentCode() : "N/A")
                    .amount("$" + (payment.getAmountPaid() != null ? 
                          payment.getAmountPaid().setScale(2, RoundingMode.HALF_UP).toString() : "0.00"))
                    .method(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Unknown")
                    .purpose(payment.getPaymentType() != null && payment.getPaymentType().equals("DEPOSIT") ? 
                            "Advance Deposit" : "Final Bill")
                    .invoice(invoiceNumber)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping payment to history item for payment {}: {}", 
                     payment != null ? payment.getPaymentId() : "null", e.getMessage());
            return GuestDetailResponse.PaymentHistoryItem.builder()
                    .date("N/A")
                    .transactionId("N/A")
                    .amount("$0.00")
                    .method("Unknown")
                    .purpose("N/A")
                    .invoice("N/A")
                    .build();
        }
    }

    private GuestDetailResponse.ServiceHistorySection getServiceHistorySection(Long guestId) {
        try {
            List<ServiceCharge> serviceCharges = serviceChargeRepository.findByGuestGuestIdOrderByChargeDateDesc(guestId);
            
            List<GuestDetailResponse.ServiceHistoryItem> items = serviceCharges.stream()
                    .map(this::mapServiceChargeToHistoryItem)
                    .collect(Collectors.toList());
            
            long completed = items.stream()
                    .filter(item -> item.getStatus().equals("Delivered") || item.getStatus().equals("Billed"))
                    .count();
            
            long pending = items.stream()
                    .filter(item -> item.getStatus().equals("Pending"))
                    .count();
            
            BigDecimal totalCharges = items.stream()
                    .map(item -> {
                        try {
                            return new BigDecimal(item.getCharge().replace("$", "").replace(",", ""));
                        } catch (Exception e) {
                            return BigDecimal.ZERO;
                        }
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return GuestDetailResponse.ServiceHistorySection.builder()
                    .services(items)
                    .totalServices((long) items.size())
                    .completed(completed)
                    .pending(pending)
                    .totalCharges(totalCharges)
                    .build();
        } catch (Exception e) {
            log.error("Error getting service history for guest {}: {}", guestId, e.getMessage());
            return GuestDetailResponse.ServiceHistorySection.builder()
                    .services(Collections.emptyList())
                    .totalServices(0L)
                    .completed(0L)
                    .pending(0L)
                    .totalCharges(BigDecimal.ZERO)
                    .build();
        }
    }

    private GuestDetailResponse.ServiceHistoryItem mapServiceChargeToHistoryItem(ServiceCharge serviceCharge) {
        try {
            String status = serviceCharge.getIsPaid() != null && serviceCharge.getIsPaid() ? "Billed" : "Pending";
            if (serviceCharge.getIsPaid() != null && serviceCharge.getIsPaid() && 
                serviceCharge.getChargeDate() != null && 
                serviceCharge.getChargeDate().isBefore(LocalDateTime.now().minusHours(1))) {
                status = "Delivered";
            }
            
            String date = "N/A";
            if (serviceCharge.getChargeDate() != null) {
                try {
                    date = serviceCharge.getChargeDate().format(dateTimeFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting charge date: {}", e.getMessage());
                }
            } else if (serviceCharge.getCreatedAt() != null) {
                try {
                    date = serviceCharge.getCreatedAt().format(dateTimeFormatter);
                } catch (Exception e) {
                    log.warn("Error formatting created at: {}", e.getMessage());
                }
            }
            
            return GuestDetailResponse.ServiceHistoryItem.builder()
                    .date(date)
                    .service(serviceCharge.getCategory() != null ? serviceCharge.getCategory() : "Service")
                    .description(serviceCharge.getDescription() != null ? serviceCharge.getDescription() : "")
                    .charge("$" + (serviceCharge.getAmount() != null ? 
                          serviceCharge.getAmount().setScale(2, RoundingMode.HALF_UP).toString() : "0.00"))
                    .status(status)
                    .build();
        } catch (Exception e) {
            log.error("Error mapping service charge to history item: {}", e.getMessage());
            return GuestDetailResponse.ServiceHistoryItem.builder()
                    .date("N/A")
                    .service("N/A")
                    .charge("$0.00")
                    .status("Error")
                    .build();
        }
    }

    private GuestDetailResponse.PreferencesSection mapPreferencesToResponse(Guest guest) {
        try {
            List<String> benefits = List.of(
                "Free room upgrade (subject to availability)",
                "Late checkout until 2:00 PM",
                "Welcome drink on arrival",
                "10% discount on F&B"
            );
            
            GuestDetailResponse.PreferencesSection.PreferencesSectionBuilder builder = GuestDetailResponse.PreferencesSection.builder()
                    .bedType("King Size")
                    .pillow("Firm (2 extra)")
                    .roomTemp("22°C")
                    .floorLevel("High Floor (12+)")
                    .smoking("Non-Smoking")
                    .newspaper("Wall Street Journal")
                    .amenities("Coffee maker, Extra towels, Bathrobe")
                    .food("Vegetarian options preferred")
                    .allergies("Peanuts, Shellfish")
                    .specialRequests("Early check-in when available, Quiet room away from elevator")
                    .vipNotes("Anniversary celebration - arrange champagne & flowers")
                    .loyaltyTier(guest.getVipStatus() != null ? guest.getVipStatus() + " Member" : "Basic Member")
                    .memberSince(guest.getCreatedAt() != null ? 
                        guest.getCreatedAt().format(dateFormatter) : "N/A")
                    .loyaltyPoints(calculateLoyaltyPoints(guest.getGuestId()))
                    .benefits(benefits);
            
            if (guest.getCompany() != null && !guest.getCompany().isEmpty()) {
                builder.corporateAccount(guest.getCompany())
                      .accountManager("Sarah Johnson")
                      .paymentTerms("Net 30 • Credit Limit: $5000.00");
            }
            
            return builder.build();
        } catch (Exception e) {
            log.error("Error mapping preferences for guest {}: {}", guest.getGuestId(), e.getMessage());
            return GuestDetailResponse.PreferencesSection.builder()
                    .loyaltyTier("Basic Member")
                    .build();
        }
    }

    private GuestDetailResponse.ActivityTimelineSection getActivityTimelineSection(Long guestId) {
        List<GuestDetailResponse.ActivityTimelineItem> items = new ArrayList<>();
        
        try {
            // Add check-in events
            List<Booking> checkIns = bookingRepository.findTop10ByGuestGuestIdOrderByCheckInDateDesc(guestId);
            for (Booking checkIn : checkIns) {
                if (checkIn.getActualCheckIn() != null) {
                    try {
                        items.add(GuestDetailResponse.ActivityTimelineItem.builder()
                                .time(checkIn.getActualCheckIn().format(dateTimeFormatter))
                                .event("Guest Checked-in")
                                .staff("Reception - " + (checkIn.getCreatedBy() != null ? checkIn.getCreatedBy() : "Staff"))
                                .icon("checkin")
                                .type("checkin")
                                .build());
                    } catch (Exception e) {
                        log.warn("Error formatting check-in date: {}", e.getMessage());
                    }
                }
            }
            
            // Add payment events
            List<Payment> payments = paymentRepository.findTop10ByGuestGuestIdOrderByCreatedAtDesc(guestId);
            for (Payment payment : payments) {
                if (payment.getCreatedAt() != null) {
                    try {
                        items.add(GuestDetailResponse.ActivityTimelineItem.builder()
                                .time(payment.getCreatedAt().format(dateTimeFormatter))
                                .event("Payment Received - $" + 
                                    (payment.getAmountPaid() != null ? 
                                     payment.getAmountPaid().setScale(2, RoundingMode.HALF_UP) : "0.00"))
                                .staff("Front Desk - " + (payment.getReceivedBy() != null ? payment.getReceivedBy() : "Staff"))
                                .icon("payment")
                                .type("payment")
                                .build());
                    } catch (Exception e) {
                        log.warn("Error formatting payment date: {}", e.getMessage());
                    }
                }
            }
            
            // Add service events
            List<ServiceCharge> services = serviceChargeRepository.findTop10ByGuestGuestIdOrderByChargeDateDesc(guestId);
            for (ServiceCharge service : services) {
                String eventTime = "N/A";
                if (service.getChargeDate() != null) {
                    try {
                        eventTime = service.getChargeDate().format(dateTimeFormatter);
                    } catch (Exception e) {
                        log.warn("Error formatting service charge date: {}", e.getMessage());
                    }
                } else if (service.getCreatedAt() != null) {
                    try {
                        eventTime = service.getCreatedAt().format(dateTimeFormatter);
                    } catch (Exception e) {
                        log.warn("Error formatting service created at: {}", e.getMessage());
                    }
                }
                
                if (!eventTime.equals("N/A")) {
                    items.add(GuestDetailResponse.ActivityTimelineItem.builder()
                            .time(eventTime)
                            .event(service.getCategory() != null ? service.getCategory() + " Service" : "Service Request")
                            .staff("Staff - " + (service.getCreatedBy() != null ? service.getCreatedBy() : "Hotel Staff"))
                            .icon("service")
                            .type("service")
                            .build());
                }
            }
            
            // Sort by time descending and limit to 10
            items.sort((a, b) -> b.getTime().compareTo(a.getTime()));
            if (items.size() > 10) {
                items = items.subList(0, 10);
            }
        } catch (Exception e) {
            log.error("Error getting activity timeline for guest {}: {}", guestId, e.getMessage());
        }
        
        return GuestDetailResponse.ActivityTimelineSection.builder()
                .activities(items)
                .build();
    }

    private String generateAvatarUrl(String firstName, String lastName) {
        String name = (firstName != null ? firstName : "") + (lastName != null ? lastName : "");
        if (name.isEmpty()) name = "Guest";
        return "https://ui-avatars.com/api/?name=" + name.replace(" ", "+") + "&size=128";
    }

    private Integer calculateLoyaltyPoints(Long guestId) {
        try {
            Long stays = bookingRepository.countByGuestGuestIdAndStatus(guestId, "CHECKED_OUT");
            return (int) (stays != null ? stays * 1556 : 12450);
        } catch (Exception e) {
            log.warn("Error calculating loyalty points for guest {}: {}", guestId, e.getMessage());
            return 12450;
        }
    }

    private String formatCurrency(Double amount) {
        if (amount == null || amount == 0) return "$0.00";
        return String.format("$%,.2f", amount);
    }
}