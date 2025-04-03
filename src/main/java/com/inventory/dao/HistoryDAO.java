package com.inventory.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class HistoryDAO implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(HistoryDAO.class);
    private final Connection connection;

    public HistoryDAO(Connection connection) {
        this.connection = connection;
    }

    public void logProductHistory(int productId, String action, String oldValue, String newValue) {
        String query = "INSERT INTO ProductHistory (product_id, action, old_value, new_value) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, productId);
            stmt.setString(2, action);
            stmt.setObject(3, oldValue, Types.VARCHAR);
            stmt.setObject(4, newValue, Types.VARCHAR);
            stmt.executeUpdate();
            logger.debug("Logged history for product ID: {}, action: {}, old: {}, new: {}", productId, action, oldValue, newValue);
        } catch (SQLException e) {
            logger.error("Error logging product history: {}", e.getMessage(), e);
            throw new RuntimeException("Error logging product history: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        logger.debug("Closing HistoryDAO.");
        // Connection closed by caller (Main)
    }
}