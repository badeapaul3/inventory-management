package com.inventory.database;

import com.inventory.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static final String URL = ConfigManager.getInstance().getProperty("db.url", "jdbc:sqlite:inventory.sqlite");
    private static final String DRIVER = "org.sqlite.JDBC";
    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            Class.forName(DRIVER);
            logger.debug("SQLite driver loaded successfully.");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load SQLite driver: {}", e.getMessage(), e);
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
        Connection conn = DriverManager.getConnection(URL);
        logger.debug("Database connection established.");
        return conn;
    }
}