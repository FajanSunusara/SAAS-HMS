package application.services;

import application.services.dao.DatabaseManager;
import org.mindrot.jbcrypt.BCrypt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:h2:./data/hms;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found. Add h2.jar to classpath. " + e.getMessage());
            throw new SQLException("H2 Driver not found", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * Run at application startup to create tables if they don't exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {

            // Check if the USERS table is already created
            boolean isInitialized = false;
            try (ResultSet rs = conn.getMetaData().getTables(null, "PUBLIC", "USERS", null)) {
                isInitialized = rs.next();
            }

            if (!isInitialized) {
                System.out.println("[DB] Initializing database schema and data...");
                
                // Execute schema
                executeResourceScript(conn, "/db/schema.sql");
                System.out.println("[DB] Schema created successfully.");
                
                // Execute sample data
                executeResourceScript(conn, "/db/data.sql");
                System.out.println("[DB] Sample data inserted successfully.");
            } else {
                System.out.println("[DB] Database already initialized, skipping setup.");
            }
            
            // This ensures the admin user is always present,
            // even if data.sql was not run for some reason.
            ensureAdminUserExists(conn);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    private static void executeResourceScript(Connection conn, String resourcePath) throws Exception {
        InputStream in = DatabaseConnection.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("SQL resource not found: " + resourcePath);
        }
        
        String sql = new BufferedReader(new InputStreamReader(in))
                .lines().collect(Collectors.joining("\n"));
        
        try (Statement st = conn.createStatement()) {
            String[] statements = sql.split(";");
            for (String stmt : statements) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    st.execute(trimmed);
                }
            }
        }
    }

    /**
     * Checks if the default 'admin' user exists, and creates it if not.
     * Uses BCrypt for secure password hashing.
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
                    ps.setString(5, "admin@hotel.com");
                    ps.setString(6, "ACTIVE");
                    ps.executeUpdate();
                    System.out.println("[DB] Default 'admin' user created (password: admin123). Please change immediately!");
                }
            } else {
                System.out.println("[DB] Default 'admin' user already exists.");
            }
        } catch (SQLException e) {
            System.err.println("Error ensuring admin user exists: " + e.getMessage());
        }
    }
}