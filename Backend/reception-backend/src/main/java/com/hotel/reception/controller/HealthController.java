package com.hotel.reception.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health Check", description = "System health monitoring")

public class HealthController {
    
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    
    @Operation(summary = "Complete health check")
    @GetMapping
    public Map<String, Object> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // Database health
        try {
            new JdbcTemplate(dataSource).execute("SELECT 1");
            health.put("database", "UP");
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("databaseError", e.getMessage());
        }
        
        // Redis health
        try {
            redisConnectionFactory.getConnection().ping();
            health.put("redis", "UP");
        } catch (Exception e) {
            health.put("redis", "DOWN");
            health.put("redisError", e.getMessage());
        }
        
        health.put("status", "OK");
        health.put("timestamp", Instant.now().toString());
        health.put("application", "Hotel Reception Backend");
        health.put("version", "1.0.0");
        
        return health;
    }
}
