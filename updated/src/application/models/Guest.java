package application.models;

import javafx.collections.ObservableList;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class Guest {

    // Core DB fields (map to schema: guests table)
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    // Note: schema uses id_type and id_number. These two fields are UI-side; map accordingly in DAO.
    private String idProof;     // maps to guests.id_type
    private String idNumber;    // maps to guests.id_number
    // Not present in schema; keep nullable for UI compatibility if needed
    private Date dateOfBirth;   // optional; not in schema
    private Timestamp createdAt; // optional; not in schema (users has last_login, guests has no created_at)
    private String nationality;
    // UI/context fields for Home/Popup
    private String roomNumber;
    private String GstNumber;
    public String getGstNumber() {
		return GstNumber;
	}

	public void setGstNumber(String gstNumber) {
		GstNumber = gstNumber;
	}

	private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfPeople;
    private double pendingAmount;
    public double getStayAmount() {
		return StayAmount;
	}

	public void setStayAmount(double stayAmount) {
		StayAmount = stayAmount;
	}

	private double StayAmount ;


	private double advancePaid;
    public double getAdvancePaid() {
		return advancePaid;
	}

	public void setAdvancePaid(double advancePaid) {
		this.advancePaid = advancePaid;
	}

	public double getTotalAmount() {
		return TotalAmount;
	}

	public void setTotalAmount(double totalAmount) {
		TotalAmount = totalAmount;
	}

	private double TotalAmount;
    private ObservableList<application.models.PaymentHistoryEntry> paymentHistory;

    // Constructors
    public Guest() {}

    public Guest(String name, String address, String phone, String email, String idProof, String idNumber, Date dateOfBirth) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.idProof = idProof;
        this.idNumber = idNumber;
        this.dateOfBirth = dateOfBirth;
    }

    // Constructor for HomeController/Popup context
    public Guest(String name,
                 String roomNumber,
                 LocalDate checkInDate,
                 LocalDate checkOutDate,
                 int numberOfPeople,
                 double pendingAmount,
                 double TotalAmount,
                 double advancePaid,
                 ObservableList<application.models.PaymentHistoryEntry> paymentHistory) {
        this.name = name;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.numberOfPeople = numberOfPeople;
        this.pendingAmount = pendingAmount;
        this.TotalAmount = TotalAmount;
        this.advancePaid = advancePaid;
        this.paymentHistory = paymentHistory;
    }

    // Getters and Setters (DB fields)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getIdProof() { return idProof; }
    public void setIdProof(String idProof) { this.idProof = idProof; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public Date getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(Date dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    // UI/context getters
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getNumberOfPeople() { return numberOfPeople; }
    public void setNumberOfPeople(int numberOfPeople) { this.numberOfPeople = numberOfPeople; }

    public double getPendingAmount() { return pendingAmount; }
    public void setPendingAmount(double pendingAmount) { this.pendingAmount = pendingAmount; }

    public ObservableList<application.models.PaymentHistoryEntry> getPaymentHistory() { return paymentHistory; }
    public void setPaymentHistory(ObservableList<application.models.PaymentHistoryEntry> paymentHistory) {
        this.paymentHistory = paymentHistory;
    }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }


    @Override
    public String toString() {
        return "Guest{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", idProof='" + idProof + '\'' +
                ", idNumber='" + idNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", createdAt=" + createdAt +
                ", roomNumber='" + roomNumber + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", numberOfPeople=" + numberOfPeople +
                ", pendingAmount=" + pendingAmount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Guest)) return false;
        Guest guest = (Guest) o;
        return Objects.equals(id, guest.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
