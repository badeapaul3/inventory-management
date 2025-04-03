package com.inventory.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class SupplierDAO implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(SupplierDAO.class);
    private final Connection connection;

    public SupplierDAO(Connection connection) {
        this.connection = connection;
    }

    public int insertSupplier(String name, String contactInfo) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid supplier name: {}", name);
            throw new IllegalArgumentException("Supplier name cannot be empty.");
        }
        String query = "INSERT INTO Supplier (name, contact_info) VALUES (?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setObject(2, contactInfo, Types.VARCHAR);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                logger.info("Supplier inserted, ID: {}, name: {}, contactInfo: {}", id, name, contactInfo);
                return id;
            }
            logger.error("Failed to retrieve supplier ID after insert.");
            throw new SQLException("Failed to retrieve supplier ID.");
        } catch (SQLException e) {
            logger.error("Error inserting supplier: {}", e.getMessage(), e);
            throw new RuntimeException("Error inserting supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        logger.debug("Closing SupplierDAO.");
        // Connection closed by caller (Main)
    }
}