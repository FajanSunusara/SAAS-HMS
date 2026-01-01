package application.services.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;

/**
 * Simple Database Manager for maximum H2 compatibility
 * Uses basic connection settings that work with all H2 versions
 */
public class DatabaseManager {
    
    // Simple database configuration - maximum compatibility
    private static final String JDBC_URL = "jdbc:h2:./data/hms";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * Get a simple H2 database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 Driver not found", e);
        }
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * Initialize schema and load sample data if tables are empty.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            System.out.println("[DB] Ensuring schema...");
            executeResourceScript(conn, "/db/schema.sql");

            // Load data only if main tables are empty
            if (isTableEmpty(conn, "USERS")) {
                System.out.println("[DB] Loading sample data...");
                executeResourceScript(conn, "/db/data.sql");
            } else {
                System.out.println("[DB] Sample data already exists. Skipping data load.");
            }

            conn.commit();
            System.out.println("[DB] Database initialization complete.");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a table is empty.
     */
    private static boolean isTableEmpty(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            return rs.getInt(1) == 0;
        } catch (SQLException e) {
            // Table might not exist yet; treat as empty
            return true;
        }
    }

    /**
     * Executes SQL script from resource file.
     */
    private static void executeResourceScript(Connection conn, String resourcePath) throws Exception {
        InputStream in = DatabaseManager.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IllegalStateException("SQL resource not found: " + resourcePath);
        }

        String sql = new BufferedReader(new InputStreamReader(in))
                .lines().collect(Collectors.joining("\n"));

        try (Statement st = conn.createStatement()) {
            for (String stmt : sql.split(";")) {
                String trimmed = stmt.trim();
                if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                    st.execute(trimmed);
                }
            }
        }
    }

    /**
     * Drops all tables in the current schema.
     */
    public static void resetDatabase() {
        System.out.println("[DB] Resetting database...");
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
                try (ResultSet rs = stmt.executeQuery(
                        "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA() AND TABLE_TYPE='TABLE'")) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        System.out.println("[DB] Dropping table: " + tableName);
                        stmt.execute("DROP TABLE IF EXISTS " + tableName + " CASCADE");
                    }
                }
                stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Database reset failed: " + e.getMessage(), e);
        }

        // Re-initialize
        initializeDatabase();
    }

    /**
     * Test database connection
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SELECT 1");
                System.out.println("[DB] Connection test successful");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[DB] Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Print database statistics
     */
    public static void printDatabaseStats() {
        System.out.println("=== DATABASE STATISTICS ===");
        
        String[] tables = {
            "USERS", "GUESTS", "ROOMS", "STAFF", "REFERENCE_SOURCES", 
            "SERVICES", "RESERVATIONS", "BOOKINGS", "PAYMENTS", 
            "INVOICES", "ATTENDANCE", "HOUSEKEEPING"
        };

        try (Connection conn = getConnection()) {
            for (String table : tables) {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "SELECT COUNT(*) FROM " + table)) {
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int count = rs.getInt(1);
                            System.out.printf("%-15s: %d rows%n", table, count);
                        }
                    }
                } catch (SQLException e) {
                    System.out.printf("%-15s: ERROR - %s%n", table, e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database statistics: " + e.getMessage());
        }
        
        System.out.println("=== END STATISTICS ===");
    }

    // Quick test
    public static void main(String[] args) {
        System.out.println("Testing Simple DatabaseManager...");
        
        if (args.length > 0) {
            String command = args[0].toLowerCase();
            switch (command) {
                case "init":
                    initializeDatabase();
                    break;
                case "reset":
                    resetDatabase();
                    break;
                case "stats":
                    printDatabaseStats();
                    break;
                case "test":
                    testConnection();
                    break;
                default:
                    System.out.println("Available commands: init, reset, stats, test");
            }
        } else {
            // Default behavior
            initializeDatabase();
            printDatabaseStats();
        }
    }
}