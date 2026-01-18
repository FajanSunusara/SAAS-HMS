package com.hotel.reception.model.dto.request;

import lombok.Data;

@Data
public class EmailRequest {
    private String recipientEmail;
    private String subject;
    private String message;
    private boolean includePDF;
}