// src/main/java/application/enums/UserRole.java
package application.enums;

public enum UserRole {
    MANAGER("Manager"),
    STAFF("Staff");

    private final String displayValue;

    UserRole(String displayValue) {
        this.displayValue = displayValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    @Override
    public String toString() {
        return displayValue;
    }
}
