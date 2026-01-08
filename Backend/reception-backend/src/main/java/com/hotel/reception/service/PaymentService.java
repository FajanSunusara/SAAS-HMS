package com.hotel.reception.service;

import com.hotel.reception.exception.ResourceNotFoundException;
import com.hotel.reception.model.dto.request.PaymentRequest;
import com.hotel.reception.model.dto.response.PaymentResponse;
import com.hotel.reception.model.entity.Booking;
import com.hotel.reception.model.entity.Guest;
import com.hotel.reception.model.entity.Invoice;
import com.hotel.reception.model.entity.Payment;
import com.hotel.reception.repository.BookingRepository;
import com.hotel.reception.repository.InvoiceRepository;
import com.hotel.reception.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final GuestService guestService;
    
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment of {} via {}", request.getAmountPaid(), request.getPaymentMethod());
        
        Payment payment = new Payment();
        payment.setPaymentCode(generatePaymentCode());
        payment.setPaymentType(request.getPaymentType());
        payment.setAmountPaid(request.getAmountPaid());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now());
        
        // Link to invoice
        if (request.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + request.getInvoiceId()));
            payment.setInvoice(invoice);
            
            // Update invoice payment status
            updateInvoicePaymentStatus(invoice, request.getAmountPaid());
        }
        
        // Link to booking
        if (request.getBookingId() != null) {
            Booking booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + request.getBookingId()));
            payment.setBooking(booking);
        }
        
        // Link to guest
        if (request.getGuestId() != null) {
            Guest guest = guestService.getGuestEntityById(request.getGuestId());
            payment.setGuest(guest);
        }
        
        // Set payment method specific details
        setPaymentMethodDetails(payment, request);
        
        payment.setReceivedBy(request.getReceivedBy());
        payment.setRemarks(request.getRemarks());
        payment.setStatus("COMPLETED");
        
        Payment savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully: {}", savedPayment.getPaymentCode());
        
        return mapToResponse(savedPayment);
    }
    
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));
        return mapToResponse(payment);
    }
    
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceInvoiceId(invoiceId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public List<PaymentResponse> getPaymentsByBooking(Long bookingId) {
        return paymentRepository.findByBookingBookingId(bookingId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToResponse);
    }
    
    public BigDecimal getTodayCollection() {
        LocalDate today = LocalDate.now();
        BigDecimal total = paymentRepository.getTotalCollectionByDate(today);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public Map<String, BigDecimal> getPaymentMethodBreakdown(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = paymentRepository.getPaymentMethodBreakdown(startDate, endDate);
        Map<String, BigDecimal> breakdown = new HashMap<>();
        
        for (Object[] result : results) {
            String method = (String) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            breakdown.put(method, amount);
        }
        
        return breakdown;
    }
    
    public List<PaymentResponse> getPaymentsBetweenDates(LocalDate start, LocalDate end) {
        return paymentRepository.findPaymentsBetweenDates(start, end).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    // Helper methods
    private String generatePaymentCode() {
        return "PAY-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private void setPaymentMethodDetails(Payment payment, PaymentRequest request) {
        switch (request.getPaymentMethod()) {
            case "CARD":
                payment.setTransactionId(request.getTransactionId());
                payment.setCardLastFour(request.getCardLastFour());
                payment.setCardType(request.getCardType());
                payment.setCardAuthCode(request.getCardAuthCode());
                break;
            case "UPI":
                payment.setUpiId(request.getUpiId());
                payment.setTransactionId(request.getTransactionId());
                break;
            case "BANK_TRANSFER":
                payment.setBankName(request.getBankName());
                payment.setTransactionId(request.getTransactionId());
                break;
            case "ONLINE":
                payment.setGateway(request.getGateway());
                payment.setGatewayTxnId(request.getGatewayTxnId());
                payment.setTransactionId(request.getTransactionId());
                break;
            case "CASH":
                payment.setCashDenomination(request.getCashDenomination());
                break;
        }
    }
    
    private void updateInvoicePaymentStatus(Invoice invoice, BigDecimal paymentAmount) {
        BigDecimal currentPaid = invoice.getAmountPaid() != null ? invoice.getAmountPaid() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(paymentAmount);
        invoice.setAmountPaid(newPaid);
        
        BigDecimal balanceDue = invoice.getTotalAmount().subtract(newPaid);
        invoice.setBalanceDue(balanceDue);
        
        if (balanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            invoice.setStatus("PAID");
        } else if (newPaid.compareTo(BigDecimal.ZERO) > 0) {
            invoice.setStatus("PARTIAL");
        }
        
        invoiceRepository.save(invoice);
    }
    
    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentCode(payment.getPaymentCode())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getInvoiceId() : null)
                .invoiceNumber(payment.getInvoice() != null ? payment.getInvoice().getInvoiceNumber() : null)
                .bookingId(payment.getBooking() != null ? payment.getBooking().getBookingId() : null)
                .bookingCode(payment.getBooking() != null ? payment.getBooking().getBookingCode() : null)
                .guestName(payment.getGuest() != null ? 
                    payment.getGuest().getFirstName() + " " + payment.getGuest().getLastName() : null)
                .paymentType(payment.getPaymentType())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .transactionId(payment.getTransactionId())
                .cardLastFour(payment.getCardLastFour())
                .cardType(payment.getCardType())
                .upiId(payment.getUpiId())
                .bankName(payment.getBankName())
                .gateway(payment.getGateway())
                .receivedBy(payment.getReceivedBy())
                .remarks(payment.getRemarks())
                .status(payment.getStatus())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
