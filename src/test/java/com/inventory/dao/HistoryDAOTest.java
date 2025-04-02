package com.inventory.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoryDAOTest {
    private Connection mockConnection;
    private HistoryDAO historyDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        historyDAO = new HistoryDAO(mockConnection);
    }

    @Test
    void testLogProductHistorySuccess() throws SQLException {
        // Arrange
        int productId = 1;
        String action = "UPDATE";
        String oldValue = "stock: 100";
        String newValue = "stock: 150";
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("INSERT INTO ProductHistory (product_id, action, old_value, new_value) VALUES (?, ?, ?, ?)"))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);

        // Act
        historyDAO.logProductHistory(productId, action, oldValue, newValue);

        // Assert
        verify(mockStmt).setInt(1, productId);
        verify(mockStmt).setString(2, action);
        verify(mockStmt).setObject(3, oldValue, Types.VARCHAR);
        verify(mockStmt).setObject(4, newValue, Types.VARCHAR);
        verify(mockStmt).executeUpdate();
    }

    @Test
    void testLogProductHistoryWithNullValues() throws SQLException {
        // Arrange
        int productId = 1;
        String action = "DELETE";
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("INSERT INTO ProductHistory (product_id, action, old_value, new_value) VALUES (?, ?, ?, ?)"))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);

        // Act
        historyDAO.logProductHistory(productId, action, null, null);

        // Assert
        verify(mockStmt).setObject(3, null, Types.VARCHAR);
        verify(mockStmt).setObject(4, null, Types.VARCHAR);
    }

    @Test
    void testLogProductHistorySQLExceptionThrows() throws SQLException {
        // Arrange
        int productId = 1;
        String action = "UPDATE";
        PreparedStatement mockStmt = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("INSERT INTO ProductHistory (product_id, action, old_value, new_value) VALUES (?, ?, ?, ?)"))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenThrow(new SQLException("DB error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () ->
                historyDAO.logProductHistory(productId, action, "old", "new"));
        assertTrue(exception.getMessage().contains("Error logging product history"));
    }
}