package application.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GroupBookingData {
    private Long groupId;
    private String groupName;
    private String companyName;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private String gstNumber;
    private String address;
    private String nationality = "India";
    private String documentPath;
    private String photoPath;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int totalNights;
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private BigDecimal advancePaid = BigDecimal.ZERO;
    private BigDecimal gstAmount = BigDecimal.ZERO;
    private BigDecimal baseAmount = BigDecimal.ZERO;
    private BigDecimal gstRate = new BigDecimal("18.00");
    private boolean gstIncluded = false;
    private String paymentStatus = "Pending";
    private String bookingStatus = "Confirmed";
    private String sourceWebsite = "Direct";
    private List<RoomBooking> rooms = new ArrayList<>();
    private List<GuestMember> members = new ArrayList<>();
    private List<ServiceLineItem> services = new ArrayList<>();

    // Default constructor
    public GroupBookingData() {}

    // Constructor with basic group information
    public GroupBookingData(String groupName, String contactPerson, String contactPhone, 
                          String contactEmail, String address) {
        this.groupName = groupName;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.address = address;
    }

    // Full constructor
    public GroupBookingData(String groupName, String companyName, String contactPerson, 
                          String contactPhone, String contactEmail, String gstNumber, 
                          String address, String nationality, LocalDate checkInDate, 
                          LocalDate checkOutDate) {
        this.groupName = groupName;
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.gstNumber = gstNumber;
        this.address = address;
        this.nationality = nationality != null ? nationality : "India";
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        calculateTotalNights();
    }

    // Room management methods
    public void addRoom(RoomBooking room) {
        this.rooms.add(room);
        recalculateTotals();
    }

    public void addRoom(String roomNumber, String roomType, BigDecimal ratePerNight, 
                       LocalDate checkInDate, LocalDate checkOutDate, String primaryGuest) {
        RoomBooking room = new RoomBooking(roomNumber, roomType, ratePerNight, 
                                         checkInDate, checkOutDate, primaryGuest);
        addRoom(room);
    }

    public void removeRoom(RoomBooking room) {
        this.rooms.remove(room);
        recalculateTotals();
    }

    public void removeRoom(String roomNumber) {
        rooms.removeIf(room -> roomNumber.equals(room.getRoomNumber()));
        recalculateTotals();
    }

    public RoomBooking getRoomByNumber(String roomNumber) {
        return rooms.stream()
                .filter(room -> roomNumber.equals(room.getRoomNumber()))
                .findFirst()
                .orElse(null);
    }

    // Member management methods
    public void addMember(GuestMember member) {
        this.members.add(member);
    }

    public void addMember(String name, String phone, String email, String documentType, 
                         String documentNumber, String assignedRoom) {
        GuestMember member = new GuestMember(name, phone, email, documentType, 
                                           documentNumber, assignedRoom);
        addMember(member);
    }

    public void removeMember(GuestMember member) {
        this.members.remove(member);
    }

    public void removeMember(String name) {
        members.removeIf(member -> name.equals(member.getName()));
    }

    public List<GuestMember> getMembersByRoom(String roomNumber) {
        List<GuestMember> roomMembers = new ArrayList<>();
        for (GuestMember member : members) {
            if (roomNumber.equals(member.getAssignedRoom())) {
                roomMembers.add(member);
            }
        }
        return roomMembers;
    }

    // Service management methods
    public void addService(ServiceLineItem service) {
        this.services.add(service);
        recalculateTotals();
    }

    public void addService(String serviceName, int quantity, double unitPrice) {
        ServiceLineItem service = new ServiceLineItem(serviceName, quantity, unitPrice);
        addService(service);
    }

    public void removeService(ServiceLineItem service) {
        this.services.remove(service);
        recalculateTotals();
    }

    // Total calculation methods
    private void recalculateTotals() {
        calculateBaseAmount();
        calculateTotalNights();
        calculateGST();
        calculateTotalAmount();
    }

    private void calculateBaseAmount() {
        BigDecimal roomTotal = rooms.stream()
                .map(RoomBooking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal serviceTotal = services.stream()
                .map(service -> BigDecimal.valueOf(service.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.baseAmount = roomTotal.add(serviceTotal);
    }

    private void calculateTotalNights() {
        if (checkInDate != null && checkOutDate != null && checkOutDate.isAfter(checkInDate)) {
            this.totalNights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        } else {
            this.totalNights = rooms.stream()
                    .mapToInt(RoomBooking::getNights)
                    .max()
                    .orElse(0);
        }
    }

    private void calculateGST() {
        if (gstIncluded) {
            // GST is already included in base amount, extract it
            BigDecimal gstFactor = gstRate.divide(new BigDecimal("100")).add(BigDecimal.ONE);
            BigDecimal amountWithoutGst = baseAmount.divide(gstFactor, 2, BigDecimal.ROUND_HALF_UP);
            this.gstAmount = baseAmount.subtract(amountWithoutGst);
            this.baseAmount = amountWithoutGst;
        } else {
            // GST needs to be added to base amount
            this.gstAmount = baseAmount.multiply(gstRate.divide(new BigDecimal("100")));
        }
        this.gstAmount = gstAmount.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void calculateTotalAmount() {
        this.totalAmount = baseAmount.add(gstAmount);
    }

    // Utility methods
    public int getTotalRooms() {
        return rooms.size();
    }

    public int getTotalMembers() {
        return members.size();
    }

    public int getTotalServices() {
        return services.size();
    }

    public BigDecimal getTotalServicesAmount() {
        return services.stream()
                .map(service -> BigDecimal.valueOf(service.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(advancePaid).max(BigDecimal.ZERO);
    }

    public boolean isFullyPaid() {
        return getBalanceDue().compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean hasDocuments() {
        return documentPath != null && !documentPath.trim().isEmpty();
    }

    public boolean hasPhoto() {
        return photoPath != null && !photoPath.trim().isEmpty();
    }

    public String getRoomNumbersAsString() {
        return rooms.stream()
                .map(RoomBooking::getRoomNumber)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Rooms");
    }

    public String getRoomTypesAsString() {
        return rooms.stream()
                .map(RoomBooking::getRoomType)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Rooms");
    }

    // Validation methods
    public boolean isValid() {
        return groupName != null && !groupName.trim().isEmpty() &&
               contactPerson != null && !contactPerson.trim().isEmpty() &&
               contactPhone != null && !contactPhone.trim().isEmpty() &&
               checkInDate != null && checkOutDate != null &&
               checkOutDate.isAfter(checkInDate) &&
               !rooms.isEmpty();
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        
        if (groupName == null || groupName.trim().isEmpty()) {
            errors.add("Group name is required");
        }
        
        if (contactPerson == null || contactPerson.trim().isEmpty()) {
            errors.add("Contact person is required");
        }
        
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            errors.add("Contact phone is required");
        }
        
        if (checkInDate == null) {
            errors.add("Check-in date is required");
        }
        
        if (checkOutDate == null) {
            errors.add("Check-out date is required");
        }
        
        if (checkInDate != null && checkOutDate != null && !checkOutDate.isAfter(checkInDate)) {
            errors.add("Check-out date must be after check-in date");
        }
        
        if (rooms.isEmpty()) {
            errors.add("At least one room is required");
        }
        
        return errors;
    }

    // Getters and Setters
    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
        recalculateTotals();
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
        recalculateTotals();
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
        recalculateTotals();
    }

    public int getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(int totalNights) {
        this.totalNights = totalNights;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getAdvancePaid() {
        return advancePaid;
    }

    public void setAdvancePaid(BigDecimal advancePaid) {
        this.advancePaid = advancePaid;
        recalculateTotals();
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(BigDecimal gstAmount) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public BigDecimal getGstRate() {
        return gstRate;
    }

    public void setGstRate(BigDecimal gstRate) {
        this.gstRate = gstRate;
        recalculateTotals();
    }

    public boolean isGstIncluded() {
        return gstIncluded;
    }

    public void setGstIncluded(boolean gstIncluded) {
        this.gstIncluded = gstIncluded;
        recalculateTotals();
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getSourceWebsite() {
        return sourceWebsite;
    }

    public void setSourceWebsite(String sourceWebsite) {
        this.sourceWebsite = sourceWebsite;
    }

    public List<RoomBooking> getRooms() {
        return new ArrayList<>(rooms);
    }

    public void setRooms(List<RoomBooking> rooms) {
        this.rooms = new ArrayList<>(rooms);
        recalculateTotals();
    }

    public List<GuestMember> getMembers() {
        return new ArrayList<>(members);
    }

    public void setMembers(List<GuestMember> members) {
        this.members = new ArrayList<>(members);
    }

    public List<ServiceLineItem> getServices() {
        return new ArrayList<>(services);
    }

    public void setServices(List<ServiceLineItem> services) {
        this.services = new ArrayList<>(services);
        recalculateTotals();
    }

    // equals, hashCode, and toString methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupBookingData that = (GroupBookingData) o;
        return Objects.equals(groupId, that.groupId) &&
               Objects.equals(groupName, that.groupName) &&
               Objects.equals(contactPhone, that.contactPhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupId, groupName, contactPhone);
    }

    @Override
    public String toString() {
        return "GroupBookingData{" +
                "groupId=" + groupId +
                ", groupName='" + groupName + '\'' +
                ", companyName='" + companyName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", totalRooms=" + getTotalRooms() +
                ", totalMembers=" + getTotalMembers() +
                ", totalAmount=" + totalAmount +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                '}';
    }

    // Builder pattern for fluent creation
    public static class Builder {
        private GroupBookingData groupBookingData;

        public Builder() {
            groupBookingData = new GroupBookingData();
        }

        public Builder withGroupName(String groupName) {
            groupBookingData.groupName = groupName;
            return this;
        }

        public Builder withCompanyName(String companyName) {
            groupBookingData.companyName = companyName;
            return this;
        }

        public Builder withContactPerson(String contactPerson) {
            groupBookingData.contactPerson = contactPerson;
            return this;
        }

        public Builder withContactPhone(String contactPhone) {
            groupBookingData.contactPhone = contactPhone;
            return this;
        }

        public Builder withContactEmail(String contactEmail) {
            groupBookingData.contactEmail = contactEmail;
            return this;
        }

        public Builder withGstNumber(String gstNumber) {
            groupBookingData.gstNumber = gstNumber;
            return this;
        }

        public Builder withAddress(String address) {
            groupBookingData.address = address;
            return this;
        }

        public Builder withNationality(String nationality) {
            groupBookingData.nationality = nationality;
            return this;
        }

        public Builder withDates(LocalDate checkIn, LocalDate checkOut) {
            groupBookingData.checkInDate = checkIn;
            groupBookingData.checkOutDate = checkOut;
            return this;
        }

        public Builder withDocumentPath(String documentPath) {
            groupBookingData.documentPath = documentPath;
            return this;
        }

        public Builder withPhotoPath(String photoPath) {
            groupBookingData.photoPath = photoPath;
            return this;
        }

        public Builder addRoom(RoomBooking room) {
            groupBookingData.addRoom(room);
            return this;
        }

        public Builder addMember(GuestMember member) {
            groupBookingData.addMember(member);
            return this;
        }

        public Builder addService(ServiceLineItem service) {
            groupBookingData.addService(service);
            return this;
        }

        public GroupBookingData build() {
            groupBookingData.recalculateTotals();
            return groupBookingData;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}