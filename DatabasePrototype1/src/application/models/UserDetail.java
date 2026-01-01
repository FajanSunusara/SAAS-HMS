// src/main/java/application/models/UserDetail.java
package application.models;

import application.enums.UserRole;

public class UserDetail {
    private String name;
    private UserRole designation;
    private String floor;
    private String shift;

    public UserDetail() {}

    public UserDetail(String name, UserRole designation, String floor, String shift) {
        this.name = name;
        this.designation = designation;
        this.floor = floor;
        this.shift = shift;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserRole getDesignation() { return designation; }
    public void setDesignation(UserRole designation) { this.designation = designation; }

    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }

    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    @Override
    public String toString() {
        return name; // For display in ComboBox
    }
}
