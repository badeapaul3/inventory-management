package com.inventory.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton Pattern: Ensures only ONE instance manages the database connection.
 */
public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:inventory.sqlite"; // Database URL
    private static DatabaseManager instance; // Singleton instance

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC"); // Load SQLite driver
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite driver", e);
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
