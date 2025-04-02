package com.inventory.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryDAOTest {
    private Connection mockConnection;
    private CategoryDAO categoryDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        categoryDAO = new CategoryDAO(mockConnection);
    }

    @Test
    void testInsertCategorySuccess() throws SQLException {
        // Arrange
        String categoryName = "Dairy";
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement("INSERT INTO Category (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        int generatedId = categoryDAO.insertCategory(categoryName);

        // Assert
        assertEquals(1, generatedId);
        verify(mockStmt).setString(1, categoryName);
        verify(mockStmt).executeUpdate();
    }

    @Test
    void testInsertCategoryEmptyNameThrows() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> categoryDAO.insertCategory(""));
        assertEquals("Category name cannot be empty.", exception.getMessage());
    }

    @Test
    void testInsertCategoryNullNameThrows() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> categoryDAO.insertCategory(null));
        assertEquals("Category name cannot be empty.", exception.getMessage());
    }

    @Test
    void testInsertCategorySQLExceptionThrows() throws SQLException {
        // Arrange
        String categoryName = "Dairy";
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("INSERT INTO Category (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenThrow(new SQLException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> categoryDAO.insertCategory(categoryName));
        assertTrue(exception.getMessage().contains("Error inserting category"));
        verify(mockStmt).setString(1, categoryName);
    }
}