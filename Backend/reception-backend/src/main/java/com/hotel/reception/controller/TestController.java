package com.hotel.reception.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {
    
    // Test endpoint: http://localhost:8080/hello
    @GetMapping("/hello")
    public String hello() {
        return "🏨 Hotel Management Backend is running successfully! ✅";
    }
    
    // Test endpoint: http://localhost:8080/health
    @GetMapping("/health")
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Hotel Management System");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("message", "Backend is running without database/redis");
        return health;
    }
    
    // Test endpoint: http://localhost:8080/api/test
    @GetMapping("/api/test")
    public Map<String, Object> testApi() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API Test Successful!");
        response.put("endpoints", new String[] {
            "GET /hello",
            "GET /health", 
            "GET /api/test",
            "POST /api/echo"
        });
        response.put("server", "Spring Boot 3.2.0");
        response.put("port", 8080);
        return response;
    }
    
    // Test endpoint: http://localhost:8080/api/echo
    @PostMapping("/api/echo")
    public Map<String, Object> echo(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("received", request);
        response.put("echo", "Request received successfully!");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }
}