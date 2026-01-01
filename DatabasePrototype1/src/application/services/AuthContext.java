package application.services;

import application.models.User;

public final class AuthContext {
    private static User currentUser;

    private AuthContext() {}

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getCurrentRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null && role != null && role.equalsIgnoreCase(currentUser.getRole());
    }
}
