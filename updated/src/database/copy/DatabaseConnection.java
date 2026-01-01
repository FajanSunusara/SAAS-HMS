package database.copy;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String JDBC_URL = "jdbc:h2:~/hotel_db";
    private static final String USER = "sa";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            System.out.println("Database connection successful. Initializing schema...");

            // SQL to create tables
            String createTablesSQL = createSchemaSQL();
            stmt.execute(createTablesSQL);

            System.out.println("Database schema created or updated successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error initializing the database schema.");
        }
    }

    private static String createSchemaSQL() {
        return "DROP TABLE IF EXISTS attendance; " +
               "DROP TABLE IF EXISTS housekeeping; " +
               "DROP TABLE IF EXISTS invoices; " +
               "DROP TABLE IF EXISTS payment_history; " +
               "DROP TABLE IF EXISTS booking; " +
               "DROP TABLE IF EXISTS reservations; " +
               "DROP TABLE IF EXISTS guests; " +
               "DROP TABLE IF EXISTS rooms; " +
               "DROP TABLE IF EXISTS staff; " +
               "DROP TABLE IF EXISTS services; " +
               "DROP TABLE IF EXISTS users; " +
               "DROP TABLE IF EXISTS reference_settings; " +

               "CREATE TABLE IF NOT EXISTS users (" +
               "  user_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  username VARCHAR(50) NOT NULL UNIQUE," +
               "  password VARCHAR(255) NOT NULL," +
               "  role VARCHAR(50) NOT NULL" +
               ");" +

               "CREATE TABLE IF NOT EXISTS rooms (" +
               "  room_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  room_number VARCHAR(10) NOT NULL UNIQUE," +
               "  room_category VARCHAR(50) NOT NULL," +
               "  room_rate DECIMAL(10, 2) NOT NULL," +
               "  status VARCHAR(20) NOT NULL" +
               ");" +

               "CREATE TABLE IF NOT EXISTS guests (" +
               "  guest_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  full_name VARCHAR(255) NOT NULL," +
               "  email VARCHAR(255) UNIQUE," +
               "  phone_number VARCHAR(20) UNIQUE," +
               "  address TEXT," +
               "  photo_id_path VARCHAR(255)" +
               ");" +

               "CREATE TABLE IF NOT EXISTS booking (" +
               "  booking_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  guest_id INT NOT NULL," +
               "  room_id INT NOT NULL," +
               "  check_in_date DATE NOT NULL," +
               "  check_out_date DATE NOT NULL," +
               "  total_amount DECIMAL(10, 2) NOT NULL," +
               "  amount_paid DECIMAL(10, 2) NOT NULL," +
               "  payment_status VARCHAR(50) NOT NULL," +
               "  booking_status VARCHAR(50) NOT NULL," +
               "  special_requests TEXT," +
               "  FOREIGN KEY (guest_id) REFERENCES guests(guest_id)," +
               "  FOREIGN KEY (room_id) REFERENCES rooms(room_id)" +
               ");" +

               "CREATE TABLE IF NOT EXISTS staff (" +
               "  staff_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  full_name VARCHAR(255) NOT NULL," +
               "  position VARCHAR(100) NOT NULL," +
               "  joining_date DATE NOT NULL," +
               "  contact_number VARCHAR(20)," +
               "  email VARCHAR(255)" +
               ");" +

               "CREATE TABLE IF NOT EXISTS attendance (" +
               "  attendance_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  staff_id INT NOT NULL," +
               "  attendance_date DATE NOT NULL," +
               "  is_present BOOLEAN NOT NULL," +
               "  FOREIGN KEY (staff_id) REFERENCES staff(staff_id)" +
               ");" +

               "CREATE TABLE IF NOT EXISTS housekeeping (" +
               "  housekeeping_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  room_id INT NOT NULL," +
               "  task_assigned VARCHAR(255) NOT NULL," +
               "  staff_id INT," +
               "  task_status VARCHAR(50) NOT NULL," +
               "  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  FOREIGN KEY (room_id) REFERENCES rooms(room_id)," +
               "  FOREIGN KEY (staff_id) REFERENCES staff(staff_id)" +
               ");" +
               
               "CREATE TABLE IF NOT EXISTS services (" +
               "  service_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  service_name VARCHAR(255) NOT NULL," +
               "  rate DECIMAL(10, 2) NOT NULL," +
               "  is_available BOOLEAN DEFAULT TRUE" +
               ");" +

               "CREATE TABLE IF NOT EXISTS payment_history (" +
               "  payment_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  booking_id INT NOT NULL," +
               "  amount DECIMAL(10, 2) NOT NULL," +
               "  payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
               "  payment_method VARCHAR(50)," +
               "  transaction_id VARCHAR(255)," +
               "  FOREIGN KEY (booking_id) REFERENCES booking(booking_id)" +
               ");" +
               
               "CREATE TABLE IF NOT EXISTS invoices (" +
               "  invoice_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  booking_id INT NOT NULL," +
               "  invoice_date DATE NOT NULL," +
               "  total_amount DECIMAL(10, 2) NOT NULL," +
               "  tax_amount DECIMAL(10, 2)," +
               "  FOREIGN KEY (booking_id) REFERENCES booking(booking_id)" +
               ");" +

               "CREATE TABLE IF NOT EXISTS reference_settings (" +
               "  setting_id INT AUTO_INCREMENT PRIMARY KEY," +
               "  setting_name VARCHAR(255) NOT NULL UNIQUE," +
               "  setting_value TEXT NOT NULL" +
               ");" +
                
               // Initial data for rooms and services
               "INSERT INTO rooms (room_number, room_category, room_rate, status) VALUES " +
               "('R01', 'Standard', 100.00, 'Available')," +
               "('R02', 'Deluxe', 150.00, 'Available')," +
               "('R03', 'Executive', 200.00, 'Available');" +

               "INSERT INTO services (service_name, rate) VALUES " +
               "('Laundry', 25.00)," +
               "('Extra Bed', 30.00);";
    }
}