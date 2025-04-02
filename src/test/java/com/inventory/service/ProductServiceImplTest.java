package com.inventory.service;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.HistoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ProductServiceImplTest {
    private ProductDAO mockProductDAO;
    private CategoryDAO mockCategoryDAO;
    private SupplierDAO mockSupplierDAO;
    private HistoryDAO mockHistoryDAO;
    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        mockProductDAO = mock(ProductDAO.class);
        mockCategoryDAO = mock(CategoryDAO.class);
        mockSupplierDAO = mock(SupplierDAO.class);
        mockHistoryDAO = mock(HistoryDAO.class);
        productService = new ProductServiceImpl(mockProductDAO, mockCategoryDAO, mockSupplierDAO, mockHistoryDAO);
    }

    @Test
    void testAddProductSuccess() {
        // Arrange
        Product product = new Product(0, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);

        // Act
        productService.addProduct(product);

        // Assert
        verify(mockProductDAO).insertOrUpdateProduct(product);
    }

    @Test
    void testAddProductNullThrows() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> productService.addProduct(null));
        assertEquals("Product cannot be null", exception.getMessage());
        verifyNoInteractions(mockProductDAO);
    }

    @Test
    void testApplyDiscountSuccess() throws Exception {
        // Arrange
        Product original = new Product(1, "Milk", 10.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);
        Product discounted = new Product(1, "Milk", 9.0, 100, LocalDate.of(2025, 6, 30), true, 1, 1);
        when(mockProductDAO.getAllProducts(false)).thenReturn(java.util.Collections.singletonList(original));

        // Act
        productService.applyDiscount(1);

        // Assert
        verify(mockProductDAO).updateProduct(discounted);
    }
}