package com.inventory.validation;

import com.inventory.model.Product;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * ProductValidator class to validate products using reflection and custom annotations.
 */
public class ProductValidator {

    /**
     * Validates the product based on the @ValidateProduct annotation.
     */
    public static void validateProduct(Product product) {
        // Using reflection to scan the fields of the Product class
        for (Field field : Product.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ValidateProduct.class)) {
                field.setAccessible(true);
                try {
                    Object value = field.get(product);

                    // Example validation logic for specific fields
                    if (field.getName().equals("expirationDate") && value != null) {
                        validateExpirationDate((LocalDate) value);
                    }
                    // Add other field validations as necessary

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to validate product field: " + field.getName(), e);
                }
            }
        }
    }

    /**
     * Validates the expiration date for the product.
     * For example, the expiration date must not be in the past.
     */
    private static void validateExpirationDate(LocalDate expirationDate) {
        if (expirationDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Expiration date cannot be in the past.");
        }
    }
}
