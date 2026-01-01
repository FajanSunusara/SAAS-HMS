// File: application/enums/InvoiceType.java
package application.enums;

public enum InvoiceType {
    BOOKING("Booking Invoice"),
    SERVICE("Service Invoice"),
    RESERVATION("Reservation Invoice"),
    REFUND("Refund Invoice"),
    EXPENDITURE("Expenditure Invoice");

    private final String displayName;

    InvoiceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
