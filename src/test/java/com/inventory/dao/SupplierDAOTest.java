package com.inventory.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierDAOTest {
    private Connection mockConnection;
    private SupplierDAO supplierDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        supplierDAO = new SupplierDAO(mockConnection);
    }

    @Test
    void testInsertSupplierSuccessWithContact() throws SQLException {
        // Arrange
        String name = "FarmFresh";
        String contactInfo = "555-1234";
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement("INSERT INTO Supplier (name, contact_info) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        int generatedId = supplierDAO.insertSupplier(name, contactInfo);

        // Assert
        assertEquals(1, generatedId);
        verify(mockStmt).setString(1, name);
        verify(mockStmt).setObject(2, contactInfo, Types.VARCHAR);
    }

    @Test
    void testInsertSupplierSuccessWithoutContact() throws SQLException {
        // Arrange
        String name = "FarmFresh";
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);

        when(mockConnection.prepareStatement("INSERT INTO Supplier (name, contact_info) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1);
        when(mockStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        // Act
        int generatedId = supplierDAO.insertSupplier(name, null);

        // Assert
        assertEquals(1, generatedId);
        verify(mockStmt).setString(1, name);
        verify(mockStmt).setObject(2, null, Types.VARCHAR);
    }

    @Test
    void testInsertSupplierEmptyNameThrows() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> supplierDAO.insertSupplier("", "555-1234"));
        assertEquals("Supplier name cannot be empty.", exception.getMessage());
    }
}