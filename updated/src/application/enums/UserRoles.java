package application.enums;

public enum UserRoles {
    ADMIN("Admin"),
    MANAGER("Manager"),
    HOUSEKEEPING("Housekeeping"),
    RECEPTIONIST("Receptionist");

    private final String displayValue;

    UserRoles(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }
}