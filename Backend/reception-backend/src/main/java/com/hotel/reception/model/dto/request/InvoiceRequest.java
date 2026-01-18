package com.hotel.reception.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InvoiceRequest {
    private LocalDate dueDate;
    private String notes;
    private String status;
    private BigDecimal discountAmount;
    private BigDecimal taxAmount;
    private BigDecimal otherCharges;
    private String signatureData;
    private String signatureType;
}