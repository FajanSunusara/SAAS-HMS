package application.services;

import application.models.User;
import application.services.dao.UserDAO;

public class AuthService {

    private final UserDAO userDao = new UserDAO();

    /**
     * Registers a new user with a plain text password.
     */
    public boolean registerUser(String username, String password, String role, String fullName, String email) {
        if (userDao.findByUsername(username) != null) {
            return false;
        }
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(password); // Storing plain text password
        user.setRole(role);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setStatus("ACTIVE");
        return userDao.createUser(user);
    }

    /**
     * Authenticates a user by directly comparing the provided password with the stored password hash.
     */
    public boolean authenticateUser(String username, String password) {
        User user = userDao.findByUsername(username);
        if (user == null) {
            return false;
        }
        // Direct string comparison of plain text passwords
        return password.equals(user.getPasswordHash());
    }
}