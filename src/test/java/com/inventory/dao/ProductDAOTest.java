package com.inventory.dao;

import com.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductDAOTest {
    private Connection mockConnection;
    private HistoryDAO mockHistoryDAO;
    private ProductDAO productDAO;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        mockHistoryDAO = mock(HistoryDAO.class);
        productDAO = new ProductDAO(mockConnection, mockHistoryDAO);
    }

    @Test
    void testInsertNewProduct() throws SQLException {
        Product product = new Product(0, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);

        PreparedStatement mockInsertStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(
                "INSERT INTO Product (name, price, stock, expiration_date, discounted, category_id, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        )).thenReturn(mockInsertStmt);
        when(mockInsertStmt.executeUpdate()).thenReturn(1);
        ResultSet mockGeneratedKeys = mock(ResultSet.class);
        when(mockInsertStmt.getGeneratedKeys()).thenReturn(mockGeneratedKeys);
        when(mockGeneratedKeys.next()).thenReturn(true);
        when(mockGeneratedKeys.getInt(1)).thenReturn(1);

        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckResult = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT id, stock FROM Product WHERE name = ? AND price = ? AND expiration_date = ?"))
                .thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckResult);
        when(mockCheckResult.next()).thenReturn(false);

        productDAO.insertOrUpdateProduct(product);

        verify(mockConnection).prepareStatement(
                "INSERT INTO Product (name, price, stock, expiration_date, discounted, category_id, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
        );
        verify(mockHistoryDAO).logProductHistory(1, "ADD", null, "price: 5.0, stock: 100");
    }

    @Test
    void testUpdateExistingProduct() throws SQLException {
        Product product = new Product(0, "Milk", 5.0, 50, LocalDate.of(2025, 6, 30), false, 1, 1);

        PreparedStatement mockCheckStmt = mock(PreparedStatement.class);
        ResultSet mockCheckResult = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT id, stock FROM Product WHERE name = ? AND price = ? AND expiration_date = ?"))
                .thenReturn(mockCheckStmt);
        when(mockCheckStmt.executeQuery()).thenReturn(mockCheckResult);
        when(mockCheckResult.next()).thenReturn(true);
        when(mockCheckResult.getInt("id")).thenReturn(1);
        when(mockCheckResult.getInt("stock")).thenReturn(100);

        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("UPDATE Product SET stock = stock + ?, discounted = ?, category_id = ?, supplier_id = ? WHERE id = ?"))
                .thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(1);

        productDAO.insertOrUpdateProduct(product);

        verify(mockUpdateStmt).executeUpdate();
        verify(mockHistoryDAO).logProductHistory(1, "UPDATE", "stock: 100", "stock: 150");
    }

    @Test
    void testUpdateProductSuccess() throws SQLException {
        Product product = new Product(1, "Milk", 3.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);

        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT price, stock FROM Product WHERE id = ?")).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getDouble("price")).thenReturn(5.0);
        when(mockResultSet.getInt("stock")).thenReturn(100);

        PreparedStatement mockUpdateStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("UPDATE Product SET name = ?, price = ?, stock = ?, expiration_date = ?, discounted = ?, category_id = ?, supplier_id = ? WHERE id = ?"))
                .thenReturn(mockUpdateStmt);
        when(mockUpdateStmt.executeUpdate()).thenReturn(1);

        productDAO.updateProduct(product);

        verify(mockConnection).prepareStatement("UPDATE Product SET name = ?, price = ?, stock = ?, expiration_date = ?, discounted = ?, category_id = ?, supplier_id = ? WHERE id = ?");
        verify(mockHistoryDAO).logProductHistory(1, "UPDATE", "price: 5.0, stock: 100", "price: 3.0, stock: 100");
    }

    @Test
    void testUpdateProductNotFoundThrows() throws SQLException {
        Product product = new Product(999, "Milk", 3.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockResultSet.next()).thenReturn(false);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("SELECT price, stock FROM Product WHERE id = ?")).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockResultSet);

        Exception exception = assertThrows(SQLException.class, () -> productDAO.updateProduct(product));
        assertEquals("No product found with ID: 999", exception.getMessage());
    }

    @Test
    void testDeleteProductSuccess() throws SQLException {
        PreparedStatement mockDeleteStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("DELETE FROM Product WHERE id = ?")).thenReturn(mockDeleteStmt);
        when(mockDeleteStmt.executeUpdate()).thenReturn(1);

        productDAO.deleteProduct(1);

        verify(mockDeleteStmt).executeUpdate();
        verify(mockHistoryDAO).logProductHistory(1, "DELETE", null, null);
    }

    @Test
    void testDeleteProductNotFoundThrows() throws SQLException {
        PreparedStatement mockDeleteStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("DELETE FROM Product WHERE id = ?")).thenReturn(mockDeleteStmt);
        when(mockDeleteStmt.executeUpdate()).thenReturn(0);

        Exception exception = assertThrows(SQLException.class, () -> productDAO.deleteProduct(999));
        assertEquals("No product found with ID: 999", exception.getMessage());
    }

    @Test
    void testAdjustStockSuccess() throws SQLException {
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT stock FROM Product WHERE id = ?")).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("stock")).thenReturn(100);

        PreparedStatement mockStockStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("UPDATE Product SET stock = ? WHERE id = ?")).thenReturn(mockStockStmt);
        when(mockStockStmt.executeUpdate()).thenReturn(1);

        productDAO.adjustStock(1, 50);

        verify(mockStockStmt).executeUpdate();
        verify(mockHistoryDAO).logProductHistory(1, "STOCK_ADJUST", "stock: 100", "stock: 150");
    }

    @Test
    void testAdjustStockBelowZeroThrows() throws SQLException {
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);
        when(mockConnection.prepareStatement("SELECT stock FROM Product WHERE id = ?")).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("stock")).thenReturn(100);

        Exception exception = assertThrows(IllegalStateException.class, () -> productDAO.adjustStock(1, -150));
        assertEquals("Stock cannot go below 0. Current: 100, Attempted change: -150", exception.getMessage());
    }

    @Test
    void testGetAllProductsSuccess() throws SQLException {
        ResultSet mockResult = mock(ResultSet.class);
        when(mockResult.next()).thenReturn(true).thenReturn(false);
        when(mockResult.getInt("id")).thenReturn(1);
        when(mockResult.getString("name")).thenReturn("Milk");
        when(mockResult.getDouble("price")).thenReturn(5.0);
        when(mockResult.getInt("stock")).thenReturn(100);
        when(mockResult.getString("expiration_date")).thenReturn("2025-06-30");
        when(mockResult.getBoolean("discounted")).thenReturn(false);
        when(mockResult.getObject("category_id")).thenReturn(1);
        when(mockResult.getObject("supplier_id")).thenReturn(1);

        PreparedStatement mockStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement("SELECT id, name, price, stock, expiration_date, discounted, category_id, supplier_id FROM Product"))
                .thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockResult);

        List<Product> products = productDAO.getAllProducts(false);

        assertEquals(1, products.size());
        Product product = products.get(0);
        assertEquals("Milk", product.name());
        assertEquals(5.0, product.price());
        assertEquals(100, product.stock());
    }
}