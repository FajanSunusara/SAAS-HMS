package application.services.db;

import java.sql.*;
import java.io.*;
import java.nio.file.*;

public class MigrationsRunner {

    public static void runAll() throws SQLException, IOException {
        runSQLFromFile("src/resources/db/schema.sql");
        runSQLFromFile("src/resources/db/data.sql");
    }

    private static void runSQLFromFile(String filepath) throws SQLException, IOException {
        File file = new File(filepath);
        if (!file.exists()) {
            throw new FileNotFoundException("SQL file not found: " + filepath);
        }

        String content = new String(Files.readAllBytes(file.toPath()));
        String[] statements = content.split(";");

        try (Connection conn = DatabaseManager.getConnection()) {
            for (String raw : statements) {
                String sql = raw.trim();
                if (!sql.isEmpty()) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(sql);
                    }
                }
            }
        }
    }
}
