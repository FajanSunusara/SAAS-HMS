package com.hotel.reception.service;

import com.hotel.reception.exception.ResourceNotFoundException;
import com.hotel.reception.model.dto.request.EmailRequest;
import com.hotel.reception.model.dto.request.InvoiceRequest;
import com.hotel.reception.model.dto.response.BookingResponse;
import com.hotel.reception.model.dto.response.GuestResponse;
import com.hotel.reception.model.dto.response.InvoiceResponse;
import com.hotel.reception.model.dto.response.PaymentResponse;
import com.hotel.reception.model.dto.response.RoomResponse;
import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.entity.BookingRoom;
import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.model.entity.Invoice;
import com.hotel.reception.repository.BookingRepository;
import com.hotel.reception.repository.BookingRoomRepository;
import com.hotel.reception.repository.InvoiceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class InvoiceService {
    
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final BookingRoomRepository bookingRoomRepository;
    private final GuestService guestService;
    private final PaymentService paymentService;
    private final BookingService bookingService; // This creates circular dependency
    
    // Use @Lazy to break the circular dependency
    @Autowired
    public InvoiceService(
            InvoiceRepository invoiceRepository,
            BookingRepository bookingRepository,
            BookingRoomRepository bookingRoomRepository,
            GuestService guestService,
            PaymentService paymentService,
            @Lazy BookingService bookingService) {
        this.invoiceRepository = invoiceRepository;
        this.bookingRepository = bookingRepository;
        this.bookingRoomRepository = bookingRoomRepository;
        this.guestService = guestService;
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }
    
    public InvoiceResponse generateInvoice(Long bookingId) {
        log.info("Generating invoice for booking: {}", bookingId);
        
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));
        
        // Check if invoice already exists
        invoiceRepository.findByBookingBookingId(bookingId)
                .ifPresent(invoice -> {
                    throw new RuntimeException("Invoice already exists for this booking");
                });
        
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setBooking(booking);
        invoice.setGuest(booking.getGuest());
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(7));
        
        // Calculate charges
        BigDecimal roomCharges = calculateRoomCharges(booking);
        invoice.setRoomCharges(roomCharges);
        invoice.setServiceCharges(BigDecimal.ZERO);
        invoice.setFoodCharges(BigDecimal.ZERO);
        invoice.setOtherCharges(BigDecimal.ZERO);
        
        BigDecimal subtotal = roomCharges;
        invoice.setSubtotal(subtotal);
        
        // Apply discounts
        BigDecimal discountAmount = subtotal.multiply(booking.getDiscountPercentage())
                .divide(new BigDecimal("100"))
                .add(booking.getManualDiscount());
        invoice.setDiscountAmount(discountAmount);
        
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);
        
        // Calculate tax
        BigDecimal taxAmount = afterDiscount.multiply(booking.getTaxPercentage())
                .divide(new BigDecimal("100"));
        invoice.setTaxAmount(taxAmount);
        
        BigDecimal totalAmount = afterDiscount.add(taxAmount);
        invoice.setTotalAmount(totalAmount);
        invoice.setAmountPaid(BigDecimal.ZERO);
        invoice.setBalanceDue(totalAmount);
        invoice.setStatus("PENDING");
        
        Invoice savedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice generated: {}", savedInvoice.getInvoiceNumber());
        
        return mapToResponse(savedInvoice);
    }
    
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        return mapToResponse(invoice);
    }
    
    public InvoiceResponse getInvoiceByBooking(Long bookingId) {
        Invoice invoice = invoiceRepository.findByBookingBookingId(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for booking: " + bookingId));
        return mapToResponse(invoice);
    }
    
    public List<InvoiceResponse> getInvoicesByGuest(Long guestId) {
        return invoiceRepository.findByGuestGuestId(guestId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    public List<InvoiceResponse> getPendingInvoices() {
        return invoiceRepository.findPendingInvoices().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public InvoiceResponse updateInvoiceStatus(Long invoiceId, String status) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        
        invoice.setStatus(status);
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        
        return mapToResponse(updatedInvoice);
    }
    
    public BigDecimal getTotalPendingAmount() {
        BigDecimal total = invoiceRepository.getTotalPendingAmount();
        return total != null ? total : BigDecimal.ZERO;
    }
    
    // Helper methods
    private String generateInvoiceNumber() {
        return "INV-" + LocalDate.now().getYear() + "-" + 
               String.format("%06d", System.currentTimeMillis() % 1000000);
    }
    
    private BigDecimal calculateRoomCharges(Booking booking) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingBookingId(booking.getBookingId());
        return bookingRooms.stream()
                .map(br -> br.getRoomRate().multiply(BigDecimal.valueOf(booking.getNights())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private InvoiceResponse mapToResponse(Invoice invoice) {
        // Get booking response without using bookingService if possible
        // We can get basic booking info from the booking entity directly
        Booking booking = invoice.getBooking();
        BookingResponse bookingResponse = null;
        if (booking != null) {
            // Instead of calling bookingService.getBookingById, create a minimal BookingResponse
            // Or use @Lazy as we already did, so this should work now
            bookingResponse = bookingService.getBookingById(booking.getBookingId());
        }
        
        GuestResponse guestResponse = guestService.getGuestById(invoice.getGuest().getGuestId());
        List<PaymentResponse> paymentResponses = paymentService.getPaymentsByInvoice(invoice.getInvoiceId());
        
        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .booking(bookingResponse)
                .guest(guestResponse)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .roomCharges(invoice.getRoomCharges())
                .serviceCharges(invoice.getServiceCharges())
                .foodCharges(invoice.getFoodCharges())
                .otherCharges(invoice.getOtherCharges())
                .subtotal(invoice.getSubtotal())
                .discountAmount(invoice.getDiscountAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .balanceDue(invoice.getBalanceDue())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .payments(paymentResponses)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
    
    public List<InvoiceResponse> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        return invoiceRepository.findByIssueDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<InvoiceResponse> getInvoicesByStatus(String status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        
        // Check if invoice can be deleted
        if (!invoice.getStatus().equals("PENDING")) {
            throw new RuntimeException("Only pending invoices can be deleted");
        }
        
        invoiceRepository.delete(invoice);
        log.info("Invoice deleted: {}", invoiceId);
    }
    
    
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceNumber));
        return mapToResponse(invoice);
    }

    public InvoiceResponse updateInvoice(Long invoiceId, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        
        // Update fields
        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }
        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            invoice.setStatus(request.getStatus());
        }
        if (request.getDiscountAmount() != null) {
            invoice.setDiscountAmount(request.getDiscountAmount());
        }
        if (request.getTaxAmount() != null) {
            invoice.setTaxAmount(request.getTaxAmount());
        }
        if (request.getOtherCharges() != null) {
            invoice.setOtherCharges(request.getOtherCharges());
        }
        
        // Recalculate totals if needed
        if (request.getDiscountAmount() != null || request.getTaxAmount() != null || 
            request.getOtherCharges() != null) {
            recalculateTotals(invoice);
        }
        
        Invoice updatedInvoice = invoiceRepository.save(invoice);
        log.info("Invoice updated: {}", invoiceId);
        
        return mapToResponse(updatedInvoice);
    }

    public void sendInvoiceEmail(Long invoiceId, EmailRequest emailRequest) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        
        // Implement email sending logic here
        log.info("Sending invoice {} to {}", invoice.getInvoiceNumber(), emailRequest.getRecipientEmail());
        
        // You would typically integrate with an email service
        // For now, just log the action
    }

    public byte[] generateInvoicePDF(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        
        // Implement PDF generation logic
        // This is a simplified example - you would use a PDF library like iText or Apache PDFBox
        
        log.info("Generating PDF for invoice: {}", invoice.getInvoiceNumber());
        
        // Return dummy PDF bytes for now
        String pdfContent = "PDF for Invoice " + invoice.getInvoiceNumber();
        return pdfContent.getBytes();
    }

    private void recalculateTotals(Invoice invoice) {
        BigDecimal subtotal = invoice.getRoomCharges()
                .add(invoice.getServiceCharges())
                .add(invoice.getFoodCharges())
                .add(invoice.getOtherCharges());
        
        invoice.setSubtotal(subtotal);
        
        BigDecimal afterDiscount = subtotal.subtract(invoice.getDiscountAmount());
        BigDecimal taxAmount = invoice.getTaxAmount() != null ? 
                invoice.getTaxAmount() : 
                afterDiscount.multiply(new BigDecimal("0.10")); // Default 10% tax
        
        BigDecimal totalAmount = afterDiscount.add(taxAmount);
        invoice.setTotalAmount(totalAmount);
        
        BigDecimal balanceDue = totalAmount.subtract(invoice.getAmountPaid());
        invoice.setBalanceDue(balanceDue);
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
 // Add this method to InvoiceService.java
    public Page<InvoiceResponse> getAllInvoicesOptimized(Pageable pageable) {
        // Fetch invoices with all necessary relations in one query
        Page<Invoice> invoices = invoiceRepository.findAllWithDetails(pageable);
        
        // Map to response without additional database calls
        return invoices.map(this::mapToResponseOptimized);
    }

    public InvoiceResponse getInvoiceByIdOptimized(Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdWithDetails(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));
        return mapToResponseOptimized(invoice);
    }

    // Optimized mapping method
    private InvoiceResponse mapToResponseOptimized(Invoice invoice) {
        Booking booking = invoice.getBooking();
        Guest guest = invoice.getGuest();
        
        // Create minimal booking response to avoid circular dependency
        BookingResponse bookingResponse = null;
        if (booking != null) {
            bookingResponse = BookingResponse.builder()
                    .bookingId(booking.getBookingId())
                    .bookingCode(booking.getBookingCode())
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .nights(booking.getNights())
                    .status(booking.getStatus())
                    .build();
            
            // If you need room information, fetch it separately
            if (booking.getBookingRooms() != null && !booking.getBookingRooms().isEmpty()) {
                List<RoomResponse> roomResponses = booking.getBookingRooms().stream()
                        .map(br -> RoomResponse.builder()
                                .roomNumber(br.getRoom().getRoomNumber())
                                .roomType(br.getRoom().getRoomType())
                                .roomRate(br.getRoomRate())
                                .build())
                        .collect(Collectors.toList());
                bookingResponse.setRooms(roomResponses);
            }
        }
        
        // Create guest response
        GuestResponse guestResponse = null;
        if (guest != null) {
            guestResponse = GuestResponse.builder()
                    .guestId(guest.getGuestId())
                    .firstName(guest.getFirstName())
                    .lastName(guest.getLastName())
                    .email(guest.getEmail())
                    .phone(guest.getPhone())
                    .company(guest.getCompany())
                    .build();
        }
        
        // Get payments without calling paymentService to avoid additional queries
        List<PaymentResponse> paymentResponses = invoice.getPayments() != null ?
                invoice.getPayments().stream()
                        .map(p -> PaymentResponse.builder()
                                .paymentId(p.getPaymentId())
                                .paymentMethod(p.getPaymentMethod())
                                .amountPaid(p.getAmountPaid())
                                .paymentDate(p.getPaymentDate())
                                .transactionId(p.getTransactionId())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();
        
        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .booking(bookingResponse)
                .guest(guestResponse)
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .roomCharges(invoice.getRoomCharges())
                .serviceCharges(invoice.getServiceCharges())
                .foodCharges(invoice.getFoodCharges())
                .otherCharges(invoice.getOtherCharges())
                .subtotal(invoice.getSubtotal())
                .discountAmount(invoice.getDiscountAmount())
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .amountPaid(invoice.getAmountPaid())
                .balanceDue(invoice.getBalanceDue())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .payments(paymentResponses)
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
    
    
}