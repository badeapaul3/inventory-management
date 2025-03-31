package com.inventory.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    public static void initializeDatabase() {
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {

            // Create Product table (unchanged from your version)
            String createProductSQL = """
                CREATE TABLE IF NOT EXISTS Product (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    price REAL NOT NULL,
                    stock INTEGER NOT NULL,
                    expiration_date TEXT NOT NULL,
                    discounted BOOLEAN NOT NULL DEFAULT 0
                )
            """;
            stmt.execute(createProductSQL);

            // Create Category table
            String createCategorySQL = """
                CREATE TABLE IF NOT EXISTS Category (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                )
            """;
            stmt.execute(createCategorySQL);

            // Create Supplier table
            String createSupplierSQL = """
                CREATE TABLE IF NOT EXISTS Supplier (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    contact_info TEXT
                )
            """;
            stmt.execute(createSupplierSQL);

            // Create ProductHistory table
            String createHistorySQL = """
                CREATE TABLE IF NOT EXISTS ProductHistory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    product_id INTEGER NOT NULL REFERENCES Product(id),
                    action TEXT NOT NULL,
                    old_value TEXT,
                    new_value TEXT,
                    timestamp TEXT NOT NULL DEFAULT (CURRENT_TIMESTAMP)
                )
            """;
            stmt.execute(createHistorySQL);

            System.out.println("✅ Database initialized successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        }
    }
}