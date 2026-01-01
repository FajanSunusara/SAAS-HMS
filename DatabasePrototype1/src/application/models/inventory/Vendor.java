package application.models.inventory;

import javafx.beans.property.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Vendor {
    private final IntegerProperty vendorId = new SimpleIntegerProperty();
    private final StringProperty vendorName = new SimpleStringProperty();
    private final StringProperty contactPerson = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phone = new SimpleStringProperty();
    private final StringProperty address = new SimpleStringProperty();
    private final StringProperty gstNumber = new SimpleStringProperty();
    private final StringProperty paymentTerms = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> creditLimit = new SimpleObjectProperty<>();
    private final BooleanProperty active = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Constructors
    public Vendor() {
        setCreditLimit(BigDecimal.ZERO);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    public Vendor(String vendorName, String contactPerson, String phone) {
        this();
        setVendorName(vendorName);
        setContactPerson(contactPerson);
        setPhone(phone);
    }

    // Property getters
    public IntegerProperty vendorIdProperty() { return vendorId; }
    public StringProperty vendorNameProperty() { return vendorName; }
    public StringProperty contactPersonProperty() { return contactPerson; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneProperty() { return phone; }
    public StringProperty addressProperty() { return address; }
    public StringProperty gstNumberProperty() { return gstNumber; }
    public StringProperty paymentTermsProperty() { return paymentTerms; }
    public ObjectProperty<BigDecimal> creditLimitProperty() { return creditLimit; }
    public BooleanProperty activeProperty() { return active; }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }

    // Getters and Setters
    public int getVendorId() { return vendorId.get(); }
    public void setVendorId(int vendorId) { this.vendorId.set(vendorId); }

    public String getVendorName() { return vendorName.get(); }
    public void setVendorName(String vendorName) { this.vendorName.set(vendorName); }

    public String getContactPerson() { return contactPerson.get(); }
    public void setContactPerson(String contactPerson) { this.contactPerson.set(contactPerson); }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); }

    public String getPhone() { return phone.get(); }
    public void setPhone(String phone) { this.phone.set(phone); }

    public String getAddress() { return address.get(); }
    public void setAddress(String address) { this.address.set(address); }

    public String getGstNumber() { return gstNumber.get(); }
    public void setGstNumber(String gstNumber) { this.gstNumber.set(gstNumber); }

    public String getPaymentTerms() { return paymentTerms.get(); }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms.set(paymentTerms); }

    public BigDecimal getCreditLimit() { return creditLimit.get(); }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit.set(creditLimit); }

    public boolean isActive() { return active.get(); }
    public void setActive(boolean active) { this.active.set(active); }

    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    @Override
    public String toString() {
        return vendorName.get() + " (" + contactPerson.get() + ")";
    }
}
