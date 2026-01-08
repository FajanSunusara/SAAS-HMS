package com.hotel.reception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.Arrays;

@SpringBootApplication
@EnableCaching
@EnableJpaAuditing
@EnableAsync
public class HotelReceptionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelReceptionBackendApplication.class, args);
        printStartupMessage();
        System.out.println("\n===========================================");
        System.out.println("🏨 Hotel Reception Backend Started!");
        System.out.println("===========================================");
        System.out.println("📖 Swagger UI: http://localhost:8080/api/swagger-ui.html");
        System.out.println("📡 API Docs: http://localhost:8080/api/api-docs");
        System.out.println("🔌 WebSocket: ws://localhost:8080/api/ws");
        System.out.println("❤️  Health: http://localhost:8080/api/v1/health");
        System.out.println("===========================================\n");
    }
    
    
    private static void printStartupMessage() {
        System.out.println("\n" +
            "╔══════════════════════════════════════════════════════════════════════════╗\n" +
            "║                     🏨 HOTEL MANAGEMENT SYSTEM                          ║\n" +
            "╠══════════════════════════════════════════════════════════════════════════╣\n" +
            "║ ✅ Backend started with PostgreSQL!                                     ║\n" +
            "║                                                                          ║\n" +
            "║ 🌐 Server: http://localhost:8080/api                                    ║\n" +
            "║ 🗄️  Database: PostgreSQL 15                                            ║\n" +
            "║ 📊 PgAdmin: http://localhost:5050                                       ║\n" +
            "║                                                                          ║\n" +
            "║ 📊 AVAILABLE APIs:                                                     ║\n" +
            "║   • GET  /api/guests           - Get all guests                         ║\n" +
            "║   • GET  /api/rooms            - Get all rooms                          ║\n" +
            "║   • GET  /api/bookings         - Get all bookings                       ║\n" +
            "║   • GET  /api/dashboard/stats  - Get dashboard statistics               ║\n" +
            "║                                                                          ║\n" +
            "║ ⚙️  Configuration:                                                      ║\n" +
            "║   • Database: PostgreSQL                                                ║\n" +
            "║   • Tables: Auto-created by Hibernate                                   ║\n" +
            "║   • Data: Persistent storage                                            ║\n" +
            "╚══════════════════════════════════════════════════════════════════════════╝\n");
    }
    
    
}