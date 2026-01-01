package application.enums;

public enum SidebarItem {
    // No iconCode needed when using image files
    HAMBURGER("Hamburger", null),
    HOME("Home", "/fxml1/home.fxml"),
    BOOKING("Booking", "/fxml1/Booking.fxml"),
    CHECKOUT("Checkout", "/fxml1/Checkout.fxml"),
    RESERVATION("Reservation", "/fxml1/ReservationPage.fxml"),
    INVOICE("Invoice", "/fxml1/Invoices.fxml"),
    HOUSEKEEPING("Housekeeping", "/fxml1/Housekeeping.fxml"),
    GUEST_MANAGEMENT("Guest Management", "/fxml1/GuestManagement.fxml"),
    STAFF_MANAGEMENT("Staff Management", "/fxml1/StaffManagement.fxml"),
    REPORTS("Reports", "/fxml1/Finance.fxml"),
    Inventory("Inventory", "/fxml1/Inventory.fxml"),
    SERVICES("Services", "/fxml1/Service.fxml"),
    FINANCES("Finances", "/fxml1/FinancePage.fxml"),
    LAUNDRY("Laundry", "/fxml1/Financepage1.fxml"),
    EMAILS("Emails", "/fxml1/Financepage1.fxml"),
    RESTAURENT("Restaurent", "/fxml1/Financepage1.fxml"),
    SETTINGS("Settings", "/fxml1/settings-page.fxml"),
    LOGOUT("Logout", null); // FXML path is null for action items

    private final String name;
    private final String fxmlPath;

    SidebarItem(String name, String fxmlPath) {
        this.name = name;
        this.fxmlPath = fxmlPath;
    }

    public String getName() {
        return name;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}
