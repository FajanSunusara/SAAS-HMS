package com.hotel.reception.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private BigDecimal discountAmount;
    private String discountType; // PERCENTAGE or FLAT
    private String discountReason;
    private String paymentMethod;
    private String referenceNumber;
    private Boolean markRoomVacant;
    private Boolean triggerHousekeeping;
    private Boolean generateInvoice;
    private Boolean sendInvoice;
    private String additionalNotes;
}