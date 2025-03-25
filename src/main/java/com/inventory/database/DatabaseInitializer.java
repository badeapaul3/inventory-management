package com.inventory.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Responsible for initializing the database structure.
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        String createProductTable = """
            CREATE TABLE IF NOT EXISTS Product (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                expiration_date TEXT NOT NULL
            );
        """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createProductTable);
            System.out.println("✅ Database initialized successfully!");

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error initializing database", e);
        }
    }
}
