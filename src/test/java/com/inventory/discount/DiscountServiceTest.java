package com.inventory.discount;

import com.inventory.dao.ProductDAO;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DiscountServiceTest {
    private ProductDAO mockProductDAO;
    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        mockProductDAO = mock(ProductDAO.class);
        discountService = new DiscountService(mockProductDAO);
    }

    @Test
    void testApplyDiscountPercentage() {
        // Arrange
        Product product = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        DiscountStrategy strategy = new PercentageDiscountStrategy(BigDecimal.valueOf(20));

        // Act
        Product discounted = discountService.applyDiscount(product, strategy);

        // Assert
        assertEquals(8.0, discounted.price(), 0.01); // 10.0 * (1 - 0.2) = 8.0
        assertTrue(discounted.discounted());
        assertEquals(product.id(), discounted.id());
        assertEquals(product.name(), discounted.name());
    }

    @Test
    void testApplyDiscountNegativeResult() {
        // Arrange
        Product product = new Product(1, "Milk", 2.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        DiscountStrategy strategy = new FlatDiscountStrategy(BigDecimal.valueOf(5.0)); // Would go below 0

        // Act
        Product discounted = discountService.applyDiscount(product, strategy);

        // Assert
        assertEquals(0.0, discounted.price(), 0.01); // Capped at 0
        assertTrue(discounted.discounted());
    }

    @Test
    void testApplyAndSaveDiscountSuccess() throws SQLException {
        // Arrange
        Product product = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        DiscountStrategy strategy = new PercentageDiscountStrategy(BigDecimal.valueOf(20));
        Product discounted = new Product(1, "Milk", 8.0, 100, LocalDate.now().plusDays(10), true, 1, 1);

        // Act
        discountService.applyAndSaveDiscount(product, strategy);

        // Assert
        verify(mockProductDAO).updateProduct(discounted);
    }

    @Test
    void testApplyAndSaveDiscountNotFound() throws SQLException {
        // Arrange
        Product product = new Product(1, "Milk", 10.0, 100, LocalDate.now().plusDays(10), false, 1, 1);
        DiscountStrategy strategy = new PercentageDiscountStrategy(BigDecimal.valueOf(20));
        doThrow(new SQLException("No product found with ID: 1")).when(mockProductDAO).updateProduct(any());

        // Act & Assert
        Exception exception = assertThrows(ProductNotFoundException.class, () ->
                discountService.applyAndSaveDiscount(product, strategy));
        assertEquals("Product with ID 1 not found", exception.getMessage());
    }
}