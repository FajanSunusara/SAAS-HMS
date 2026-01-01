package application.services;

import application.utils.PropertyReader;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class GstCalculationService {
    
    private static GstCalculationService instance;
    private final PropertyReader propertyReader;
    
    private GstCalculationService() {
        this.propertyReader = PropertyReader.getInstance();
    }
    
    public static GstCalculationService getInstance() {
        if (instance == null) {
            synchronized (GstCalculationService.class) {
                if (instance == null) {
                    instance = new GstCalculationService();
                }
            }
        }
        return instance;
    }
    
    public static class GstResult {
       
		private final BigDecimal baseAmount;
        private final BigDecimal taxAmount;
        private final BigDecimal totalAmount;
        private final double gstRate;
        private final boolean gstIncluded;
        
        public GstResult(BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal totalAmount, double gstRate, boolean gstIncluded) {
            this.baseAmount = baseAmount;
            this.taxAmount = taxAmount;
            this.totalAmount = totalAmount;
            this.gstRate = gstRate;
            this.gstIncluded = gstIncluded;
        }
        
        // Getters
        public BigDecimal getBaseAmount() { return baseAmount; }
        public BigDecimal getTaxAmount() { return taxAmount; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public double getGstRate() { return gstRate; }
        public boolean isGstIncluded() { return gstIncluded; }
    }
    
    /**
     * Calculate GST based on amount and inclusion type
     */
    public GstResult calculate(BigDecimal amount, boolean gstIncluded) {
        return calculate(amount, gstIncluded, -1);
    }
    
    /**
     * Calculate GST with custom rate (for testing/override)
     */
    public GstResult calculate(BigDecimal amount, boolean gstIncluded, double customGstRate) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new GstResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, gstIncluded);
        }
        
        double gstRate = customGstRate >= 0 ? customGstRate : propertyReader.getGstRate();
        BigDecimal gstRateDecimal = new BigDecimal(gstRate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        BigDecimal baseAmount;
        BigDecimal taxAmount;
        BigDecimal totalAmount;
        
        if (gstIncluded) {
            // GST is included in the entered amount
            totalAmount = amount;
            baseAmount = totalAmount.divide(BigDecimal.ONE.add(gstRateDecimal), 2, RoundingMode.HALF_UP);
            taxAmount = totalAmount.subtract(baseAmount);
        } else {
            // GST is excluded from the entered amount
            baseAmount = amount;
            taxAmount = baseAmount.multiply(gstRateDecimal).setScale(2, RoundingMode.HALF_UP);
            totalAmount = baseAmount.add(taxAmount);
        }
        
        return new GstResult(baseAmount, taxAmount, totalAmount, gstRate, gstIncluded);
    }
    /**
     * Calculate GST with nationality and GST number consideration
     */
    public GstResult calculateGst(BigDecimal amount, String nationality, BigDecimal gstRate, boolean gstIncluded) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new GstResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0, gstIncluded);
        }
        
        // Determine actual GST rate to use
        double effectiveGstRate;
        if (gstRate != null && gstRate.compareTo(BigDecimal.ZERO) > 0) {
            effectiveGstRate = gstRate.doubleValue();
        } else {
            effectiveGstRate = propertyReader.getGstRate();
        }
        
        // Apply GST rules based on nationality
        if (!"India".equalsIgnoreCase(nationality)) {
            // No GST for foreign nationals
            effectiveGstRate = 0.0;
        }
        
        BigDecimal gstRateDecimal = new BigDecimal(effectiveGstRate).divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        
        BigDecimal baseAmount;
        BigDecimal taxAmount;
        BigDecimal totalAmount;
        
        if (gstIncluded) {
            // GST is included in the entered amount
            totalAmount = amount;
            baseAmount = totalAmount.divide(BigDecimal.ONE.add(gstRateDecimal), 2, RoundingMode.HALF_UP);
            taxAmount = totalAmount.subtract(baseAmount);
        } else {
            // GST is excluded from the entered amount
            baseAmount = amount;
            taxAmount = baseAmount.multiply(gstRateDecimal).setScale(2, RoundingMode.HALF_UP);
            totalAmount = baseAmount.add(taxAmount);
        }
        
        return new GstResult(baseAmount, taxAmount, totalAmount, effectiveGstRate, gstIncluded);
    }

    /**
     * Simplified GST calculation for service payments
     */
    public GstResult calculateGst(BigDecimal amount, String nationality, boolean hasGstNumber) {
        // For service payments, GST is typically excluded from the base amount
        return calculateGst(amount, nationality, new BigDecimal("18.00"), false);
    }
    /**
     * Calculate for multiple nights
     */
    public GstResult calculateForNights(BigDecimal ratePerNight, int nights, boolean gstIncluded) {
        BigDecimal totalRoomAmount = ratePerNight.multiply(new BigDecimal(nights));
        return calculate(totalRoomAmount, gstIncluded);
    }
    
    /**
     * Refresh GST rate from properties (call when settings change)
     */
    public void refreshConfig() {
        propertyReader.reload();
    }
}
