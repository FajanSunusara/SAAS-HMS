package application.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Objects;

public class Staff {
    private Long id;
    private String name;
    private String position;
    private String department;
    private String phone;
    private String email;
    private Date hireDate;
    private Timestamp createdAt;
    
    // Missing fields for UI
    private Integer floor;
    private Boolean isActive = true;
    private int daysActive = 0;
    private int daysPresent = 0;
    private int daysAbsent = 0;

    public Staff() {}

    public Staff(String name, String position, String department, String phone, String email, Date hireDate) {
        this.name = name;
        this.position = position;
        this.department = department;
        this.phone = phone;
        this.email = email;
        this.hireDate = hireDate;
        this.isActive = true;
    }

    // All getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getRole() { return position; }
    public void setRole(String role) { this.position = role; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Date getHireDate() { return hireDate; }
    public void setHireDate(Date hireDate) { this.hireDate = hireDate; }
    
    public Date getJoinDate() { return hireDate; }
    public void setJoinDate(Date joinDate) { this.hireDate = joinDate; }
    
    public LocalDate getJoinDateAsLocalDate() {
        return hireDate != null ? hireDate.toLocalDate() : null;
    }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public boolean isActive() { return isActive != null && isActive; }
    
    public int getDaysActive() { return daysActive; }
    public void setDaysActive(int daysActive) { this.daysActive = daysActive; }
    
    public int getDaysPresent() { return daysPresent; }
    public void setDaysPresent(int daysPresent) { this.daysPresent = daysPresent; }
    
    public int getDaysAbsent() { return daysAbsent; }
    public void setDaysAbsent(int daysAbsent) { this.daysAbsent = daysAbsent; }
    
    public String getStatus() {
        return isActive() ? "Active" : "Inactive";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Staff staff = (Staff) o;
        return Objects.equals(id, staff.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
