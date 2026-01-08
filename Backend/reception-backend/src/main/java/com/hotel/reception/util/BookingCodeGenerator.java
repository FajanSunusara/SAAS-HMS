package com.hotel.reception.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class BookingCodeGenerator {
    
    private static final String PREFIX = "BKG";
    private static final Random random = new Random();
    
    public static String generate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDateTime.now().format(formatter);
        int randomPart = 1000 + random.nextInt(9000); // 4-digit random number
        
        return PREFIX + "-" + datePart + "-" + randomPart;
    }
}
