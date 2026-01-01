package application.models;

public class CommissionDetails {
    private String sourceName; // Reference to the source
    private double commissionPercentage;
    private double fixedAmount;
    private String notes;

    public CommissionDetails(String sourceName, double commissionPercentage, double fixedAmount) {
        this.sourceName = sourceName;
        this.commissionPercentage = commissionPercentage;
        this.fixedAmount = fixedAmount;
    }

    // Getters and Setters
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public double getCommissionPercentage() { return commissionPercentage; }
    public void setCommissionPercentage(double commissionPercentage) { this.commissionPercentage = commissionPercentage; }

    public double getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(double fixedAmount) { this.fixedAmount = fixedAmount; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}