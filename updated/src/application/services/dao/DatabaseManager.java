package application.services.dao;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.stream.Collectors;
import java.io.File;

/**
 * Fixed Database Manager for EXE deployment
 * Compatible with modern H2 versions
 */
public class DatabaseManager {
    
    // ✅ FIXED: Simplified H2 connection string for modern versions
    private static final String DB_PATH = getWritableDatabasePath();
    private static final String JDBC_URL = "jdbc:h2:file:" + DB_PATH + ";DB_CLOSE_ON_EXIT=FALSE";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    /**
     * ✅ FIXED: Get writable database path that works in EXE deployment
     */
    private static String getWritableDatabasePath() {
        // Try multiple locations in order of preference
        String[] possiblePaths = {
            System.getProperty("user.dir") + "/data/hms",  // Current directory
            System.getProperty("user.home") + "/HotelMS/data/hms",  // User home
            "./data/hms"  // Relative path as fallback
        };
        
        for (String path : possiblePaths) {
            File dataDir = new File(path).getParentFile();
            if (dataDir != null) {
                dataDir.mkdirs(); // Create directory if it doesn't exist
                if (dataDir.exists() && dataDir.canWrite()) {
                    System.out.println("[DB] Using database path: " + path);
                    return path;
                }
            }
        }
        
        // Last resort
        String fallbackPath = "./data/hms";
        new File(fallbackPath).getParentFile().mkdirs();
        System.out.println("[DB] Using fallback database path: " + fallbackPath);
        return fallbackPath;
    }

    /**
     * Get a simple H2 database connection
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 Driver not found", e);
        }
        System.out.println("[DB] Connecting to: " + JDBC_URL);
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    /**
     * Initialize schema and load sample data if tables are empty.
     */
    public static void initializeDatabase() {
        // ✅ FIXED: Ensure data directory exists before connection
        ensureDataDirectory();
        
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
            System.err.println("[DB] ERROR: Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            
            // ✅ FIXED: Show user-friendly error for EXE deployment
            showDatabaseError(e);
            throw new RuntimeException("Database initialization failed: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ FIXED: Ensure data directory exists and is writable
     */
    private static void ensureDataDirectory() {
        File dataDir = new File(DB_PATH).getParentFile();
        if (dataDir != null) {
            if (!dataDir.exists()) {
                boolean created = dataDir.mkdirs();
                System.out.println("[DB] Data directory created: " + created + " at " + dataDir.getAbsolutePath());
            }
            
            // Test write permission
            File testFile = new File(dataDir, "write_test.tmp");
            try {
                testFile.createNewFile();
                testFile.delete();
                System.out.println("[DB] Write test successful in: " + dataDir.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("[DB] WARNING: Cannot write to data directory: " + dataDir.getAbsolutePath());
            }
        }
    }

    /**
     * ✅ FIXED: Show user-friendly database error
     */
    private static void showDatabaseError(Exception e) {
        // This will be visible in logs when running as EXE
        System.err.println("=========================================");
        System.err.println("DATABASE ERROR - Please check:");
        System.err.println("1. Write permissions in installation folder");
        System.err.println("2. Antivirus is not blocking H2 database");
        System.err.println("3. Disk space is available");
        System.err.println("Error details: " + e.getMessage());
        System.err.println("=========================================");
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
     * Get database file location for debugging
     */
    public static String getDatabaseLocation() {
        return DB_PATH;
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
        System.out.println("Testing Fixed DatabaseManager...");
        
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