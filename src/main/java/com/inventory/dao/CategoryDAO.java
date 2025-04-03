package com.inventory.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CategoryDAO implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(CategoryDAO.class);
    private final Connection connection;

    public CategoryDAO(Connection connection) {
        this.connection = connection;
    }

    public int insertCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid category name: {}", name);
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String query = "INSERT INTO Category (name) VALUES (?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                logger.info("Category inserted, ID: {}, name: {}", id, name);
                return id;
            }
            logger.error("Failed to retrieve category ID after insert.");
            throw new SQLException("Failed to retrieve category ID.");
        } catch (SQLException e) {
            logger.error("Error inserting category: {}", e.getMessage(), e);
            throw new RuntimeException("Error inserting category: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SQLException {
        logger.debug("Closing CategoryDAO.");
        // Connection closed by caller (Main)
    }
}