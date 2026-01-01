package application.services;

import application.services.dao.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DbInitializer {

    public static void ensureInitialized() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement()) {

            // Check if USERS table exists
            boolean usersExists = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, "PUBLIC", "USERS", null)) {
                usersExists = rs.next();
            }

            if (!usersExists) {
                // If tables don't exist, run the schema and data scripts
                System.out.println("[DB] Schema and seed data not found. Initializing database...");
                runScript(conn, "/db/schema.sql");
                runScript(conn, "/db/data.sql");
                System.out.println("[DB] Database initialization complete.");
            } else {
                System.out.println("[DB] Database already initialized. Skipping schema creation.");
                // Check if the admin user exists, and create it if not
                ensureAdminUserExists(conn);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed.", e);
        }
    }

    private static void runScript(Connection conn, String resourcePath) throws Exception {
        InputStream in = DbInitializer.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("SQL resource not found: " + resourcePath);
        }
        String sql = new BufferedReader(new InputStreamReader(in))
                .lines().collect(Collectors.joining("\n"));

        // Split by semicolon and execute
        try (Statement st = conn.createStatement()) {
            for (String stmt : sql.split(";")) {
                String s = stmt.trim();
                if (!s.isEmpty()) {
                    st.execute(s);
                }
            }
        }
    }

    /**
     * Checks if the default 'admin' user exists, and creates it if not.
     */
    private static void ensureAdminUserExists(Connection conn) {
        String checkSql = "SELECT COUNT(*) FROM users WHERE username = 'admin'";
        String insertSql = "INSERT INTO users (username, password_hash, role, full_name, email, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(checkSql)) {
            
            if (rs.next() && rs.getInt(1) == 0) {
                // Admin user does not exist, so we create it
                String hashedPassword = BCrypt.hashpw("admin123", BCrypt.gensalt());
                
                try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                    ps.setString(1, "admin");
                    ps.setString(2, hashedPassword);
                    ps.setString(3, "ADMIN");
                    ps.setString(4, "Administrator");
                    ps.setString(5, "admin@hotelmanagement.com");
                    ps.setString(6, "ACTIVE");
                    ps.executeUpdate();
                    System.out.println("[DB] Default 'admin' user created.");
                }
            } else {
                System.out.println("[DB] Default 'admin' user already exists.");
            }
        } catch (Exception e) {
            // Log the error but do not fail the entire application startup
            System.err.println("Error ensuring admin user exists: " + e.getMessage());
        }
    }
}