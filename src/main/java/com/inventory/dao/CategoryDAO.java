package com.inventory.dao;

import java.sql.*;

public class CategoryDAO implements AutoCloseable {
    private final Connection connection;

    public CategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public int insertCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String query = "INSERT INTO Category (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to retrieve category ID.");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting category: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        // Connection closed by caller (Main)
    }
}