package com.inventory.discount;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountManagerTest {
    private ProductDAO mockProductDAO;
    private DiscountManager discountManager;

    @BeforeEach
    void setUp() {
        mockProductDAO = mock(ProductDAO.class);
        discountManager = new DiscountManager(mockProductDAO);
    }

    @Test
    void testApplyDynamicDiscountsNearExpiry() throws SQLException {
        // Arrange
        Product nearExpiry = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        Product farExpiry = new Product(2, "Bread", 5.0, 50, LocalDate.now().plusDays(60), false, 1, 1);
        when(mockProductDAO.getAllProducts(false)).thenReturn(Arrays.asList(nearExpiry, farExpiry));

        // Act
        int discountedCount = discountManager.applyDynamicDiscounts();

        // Assert
        assertEquals(1, discountedCount); // Only nearExpiry should be discounted
        verify(mockProductDAO, times(1)).updateProduct(argThat(product ->
                product.id() == 1 && product.price() == 8.0 && product.discounted() // 10.0 * (1 - 0.2) = 8.0
        ));
        verify(mockProductDAO, never()).updateProduct(argThat(product -> product.id() == 2));
    }

    @Test
    void testApplyDynamicDiscountAlreadyDiscounted() throws SQLException {
        // Arrange
        Product discounted = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), true, 1, 1);
        when(mockProductDAO.getAllProducts(false)).thenReturn(Collections.singletonList(discounted));

        // Act
        int discountedCount = discountManager.applyDynamicDiscounts();

        // Assert
        assertEquals(0, discountedCount); // No change if already discounted
        verify(mockProductDAO, never()).updateProduct(any());
    }

    @Test
    void testApplyDynamicDiscountByIdNearExpiry() throws SQLException {
        // Arrange
        Product nearExpiry = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        Product updated = new Product(1, "Milk", 8.0, 100, LocalDate.now().plusDays(10), true, 1, 1);
        when(mockProductDAO.getAllProducts(false)).thenReturn(Collections.singletonList(nearExpiry))
                .thenReturn(Collections.singletonList(updated)); // Simulate update

        // Act
        Product result = discountManager.applyDynamicDiscount(1);

        // Assert
        assertEquals(8.0, result.price(), 0.01); // 20% off
        assertTrue(result.discounted());
        verify(mockProductDAO).updateProduct(argThat(product ->
                product.id() == 1 && product.price() == 8.0 && product.discounted()
        ));
    }

    @Test
    void testApplyDynamicDiscountByIdNoDiscount() throws SQLException {
        // Arrange
        Product farExpiry = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(60), false, 1, 1);
        when(mockProductDAO.getAllProducts(false)).thenReturn(Collections.singletonList(farExpiry));

        // Act
        Product result = discountManager.applyDynamicDiscount(1);

        // Assert
        assertEquals(10.0, result.price(), 0.01); // No change
        assertFalse(result.discounted());
        verify(mockProductDAO, never()).updateProduct(any());
    }
}