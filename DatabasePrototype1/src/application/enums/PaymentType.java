// File: application/enums/PaymentType.java
package application.enums;

public enum PaymentType {
    NEW_BOOKING("New Booking Payment"),
    EXISTING_BOOKING("Existing Booking Payment"),
    RESERVATION("Reservation Payment"),
    SERVICE("Service Payment"),
    REFUND("Refund Payment"),
    EXPENDITURE("Hotel Expenditure Payment");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
