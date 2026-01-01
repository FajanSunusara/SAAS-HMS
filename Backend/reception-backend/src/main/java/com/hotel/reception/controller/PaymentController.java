package com.hotel.reception.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @GetMapping
    public ResponseEntity<?> getAllPayments() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment API will be implemented soon");
        response.put("status", "Coming soon");
        return ResponseEntity.ok(response);
    }
}