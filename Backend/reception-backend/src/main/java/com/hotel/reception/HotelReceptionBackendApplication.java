package com.hotel.reception;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@SpringBootApplication
public class HotelReceptionBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotelReceptionBackendApplication.class, args);
        printStartupMessage();
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
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",  // React frontend
            "http://localhost:4200",  // Angular frontend
            "http://localhost:5173",  // Vite frontend
            "http://localhost:8080"   // Same origin
        ));
        corsConfiguration.setAllowedHeaders(Arrays.asList(
            "Origin", "Access-Control-Allow-Origin", "Content-Type",
            "Accept", "Authorization", "X-Requested-With"
        ));
        corsConfiguration.setExposedHeaders(Arrays.asList(
            "Origin", "Content-Type", "Accept", "Authorization"
        ));
        corsConfiguration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        
        return new CorsFilter(urlBasedCorsConfigurationSource);
    }
}