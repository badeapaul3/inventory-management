package com.inventory.dao;

import java.sql.*;

public class HistoryDAO implements AutoCloseable {
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
        } catch (SQLException e) {
            throw new RuntimeException("Error logging product history: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        // Connection closed by caller (Main)
    }
}