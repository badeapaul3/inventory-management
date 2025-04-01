package com.inventory.validation;

import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;

import java.lang.reflect.Field;
import java.time.LocalDate;

/**
 * ProductValidator class to validate products using reflection and custom annotations.
 */
public class ProductValidator {

    /**
     * Validates the product based on the @ValidateProduct annotation.
     * @param product the product to validate
     * @throws IllegalArgumentException if validation fails for non-date fields
     * @throws ExpiredProductException if the expiration date is in the past and not allowed
     */
    public static void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        Field[] fields = Product.class.getDeclaredFields();
        if (fields.length == 0) {
            throw new IllegalStateException("No fields found in Product class for validation.");
        }

        for (Field field : fields) {
            if (!field.isAnnotationPresent(ValidateProduct.class)) {
                continue;
            }

            field.setAccessible(true);
            ValidateProduct annotation = field.getAnnotation(ValidateProduct.class);
            String fieldName = field.getName();

            try {
                Object value = field.get(product);

                if (annotation.notNull() && value == null) {
                    throw new IllegalArgumentException(fieldName + " cannot be null");
                }

                if (value == null) {
                    continue;
                }

                if (field.getType().equals(String.class)) {
                    validateString(fieldName, (String) value, annotation);
                } else if (field.getType().equals(Double.TYPE)) {
                    validateDouble(fieldName, (Double) value, annotation);
                } else if (field.getType().equals(Integer.TYPE) || field.getType().equals(Integer.class)) {
                    validateInteger(fieldName, (Integer) value, annotation);
                } else if (field.getType().equals(LocalDate.class)) {
                    validateLocalDate(fieldName, (LocalDate) value, annotation);
                } else if (field.getType().equals(Boolean.TYPE)) {
                    // No validation needed beyond notNull
                } else {
                    throw new IllegalStateException("Unsupported field type for validation: " + field.getType());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to validate product field: " + fieldName, e);
            }
        }
    }

    private static void validateString(String fieldName, String value, ValidateProduct annotation) {
        if (annotation.notEmpty() && value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty or whitespace only.");
        }
    }

    private static void validateDouble(String fieldName, Double value, ValidateProduct annotation) {
        if (value < annotation.minValue()) {
            throw new IllegalArgumentException(fieldName + " must be at least " + annotation.minValue());
        }
    }

    private static void validateInteger(String fieldName, Object value, ValidateProduct annotation) {
        Integer intValue = (Integer) value;
        if (intValue != null && intValue < annotation.minValue()) {
            throw new IllegalArgumentException(fieldName + " must be at least " + (int) annotation.minValue());
        }
    }

    private static void validateLocalDate(String fieldName, LocalDate value, ValidateProduct annotation) {
        if (!annotation.allowPastDate() && value.isBefore(LocalDate.now())) {
            throw new ExpiredProductException(fieldName + " cannot be in the past: " + value);
        }
    }
}