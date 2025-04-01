package com.inventory.validation;

import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {
    @Test
    void testValidProduct() {
        Product product = new Product(1, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);
        assertDoesNotThrow(() -> ProductValidator.validateProduct(product));
    }

    @Test
    void testNullProductThrows(){
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(null));
        assertEquals("Product cannot be null.", exception.getMessage());
    }

    @Test
    void testEmptyNameThrows(){
        Product product = new Product(1, "", 5.0, 100,
                LocalDate.of(2025,6,30), false, 1, 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(product));
        assertEquals("name cannot be empty or whitespace only.", exception.getMessage());
    }

    @Test
    void testNegativePriceThrows() {
        Product product = new Product(1, "Milk", -5.0, 100, LocalDate.of(2025, 6, 30), false, 1, 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(product));
        assertEquals("price must be at least 0.01", exception.getMessage());
    }

    @Test
    void testNegativeStockThrows() {
        Product product = new Product(1, "Milk", 5.0, -100, LocalDate.of(2025, 6, 30), false, 1, 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(product));
        assertEquals("stock must be at least 0", exception.getMessage());
    }

    @Test
    void testExpiredDateThrows() {
        Product product = new Product(1, "Milk", 5.0, 100, LocalDate.of(2024, 1, 1), false, 1, 1);
        Exception exception = assertThrows(ExpiredProductException.class, () -> ProductValidator.validateProduct(product));
        assertTrue(exception.getMessage().contains("expirationDate cannot be in the past"));
    }

    @Test
    void testNegativeCategoryIdThrows() {
        Product product = new Product(1, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, -1, 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(product));
        assertEquals("categoryId must be at least 1", exception.getMessage());
    }

    @Test
    void testNegativeSupplierIdThrows() {
        Product product = new Product(1, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, 1, -1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> ProductValidator.validateProduct(product));
        assertEquals("supplierId must be at least 1", exception.getMessage());
    }

    @Test
    void testNullCategoryIdAllowed() {
        Product product = new Product(1, "Milk", 5.0, 100, LocalDate.of(2025, 6, 30), false, null, 1);
        assertDoesNotThrow(() -> ProductValidator.validateProduct(product));
    }
}
