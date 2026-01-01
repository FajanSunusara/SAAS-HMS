package application.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class User {
    private Long id;
    private String username;
    private String passwordHash;
    private String role;
    private String fullName;
    private String email;
    private String status;
    private String password;
    private Timestamp createdAt;

    // JavaFX Properties for TableView binding (lazy initialization)
    private StringProperty usernameProperty;
    private StringProperty roleProperty;
    private StringProperty fullNameProperty;
    private StringProperty emailProperty;
    private StringProperty statusProperty;
    private StringProperty lastLoginProperty;

    // Constructors
    public User() {}

    public User(String username, String passwordHash, String role, String fullName, String email, String status) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    // Getters and Setters (maintain compatibility)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { 
        this.username = username; 
        if (usernameProperty != null) {
            usernameProperty.set(username);
        }
    }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { 
        this.role = role; 
        if (roleProperty != null) {
            roleProperty.set(role);
        }
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { 
        this.fullName = fullName; 
        if (fullNameProperty != null) {
            fullNameProperty.set(fullName);
        }
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { 
        this.email = email; 
        if (emailProperty != null) {
            emailProperty.set(email);
        }
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        if (statusProperty != null) {
            statusProperty.set(status);
        }
    }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { 
        this.createdAt = createdAt; 
        if (lastLoginProperty != null) {
            lastLoginProperty.set(createdAt != null ? 
                createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never");
        }
    }

    // JavaFX Property getters (lazy initialization)
    public StringProperty usernameProperty() { 
        if (usernameProperty == null) {
            usernameProperty = new SimpleStringProperty(username);
        }
        return usernameProperty; 
    }

    public StringProperty roleProperty() { 
        if (roleProperty == null) {
            roleProperty = new SimpleStringProperty(role);
        }
        return roleProperty; 
    }

    public StringProperty fullNameProperty() { 
        if (fullNameProperty == null) {
            fullNameProperty = new SimpleStringProperty(fullName);
        }
        return fullNameProperty; 
    }

    public StringProperty emailProperty() { 
        if (emailProperty == null) {
            emailProperty = new SimpleStringProperty(email);
        }
        return emailProperty; 
    }

    public StringProperty statusProperty() { 
        if (statusProperty == null) {
            statusProperty = new SimpleStringProperty(status);
        }
        return statusProperty; 
    }

    public StringProperty lastLoginProperty() { 
        if (lastLoginProperty == null) {
            lastLoginProperty = new SimpleStringProperty(createdAt != null ? 
                createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "Never");
        }
        return lastLoginProperty; 
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(username, user.username) &&
                Objects.equals(status, user.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, status);
    }
}
