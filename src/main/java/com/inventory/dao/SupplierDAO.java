package com.inventory.dao;

import java.sql.*;

public class SupplierDAO implements AutoCloseable {
    private final Connection connection;

    public SupplierDAO(Connection connection) {
        this.connection = connection;
    }

    public int insertSupplier(String name, String contactInfo) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Supplier name cannot be empty.");
        }
        String query = "INSERT INTO Supplier (name, contact_info) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setObject(2, contactInfo, Types.VARCHAR);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            throw new SQLException("Failed to retrieve supplier ID.");
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        // Connection closed by caller (Main)
    }
}