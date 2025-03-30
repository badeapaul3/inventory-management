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
     */
    public static void validateProduct(Product product) {

        if(product == null) throw new IllegalArgumentException("Product cannot be null.");

        // Using reflection to scan the fields of the Product class
        for (Field field : Product.class.getDeclaredFields()) {
            if (field.isAnnotationPresent(ValidateProduct.class)) {
                field.setAccessible(true);
                ValidateProduct annotation = field.getAnnotation(ValidateProduct.class);
                try {
                    Object value = field.get(product);

                    if(annotation.notNull() && value == null) throw new IllegalArgumentException(field.getName() + " cannot be null");

                    if(value == null) continue;

                    // Example validation logic for specific fields
                    if (field.getType().equals(String.class)) {
                        validateString(field.getName(), (String) value, annotation);
                    } else if (field.getType().equals(double.class)) {
                        validateDouble(field.getName(), (Double) value, annotation);
                    } else if (field.getType().equals(int.class)) {
                        validateInt(field.getName(), (Integer) value, annotation);
                    } else if (field.getType().equals(LocalDate.class)) {
                        validateLocalDate(field.getName(), (LocalDate) value, annotation);
                    }
                    // Add other field validations as necessary

                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to validate product field: " + field.getName(), e);
                }
            }
        }
    }


    private static void validateString(String fieldName, String value, ValidateProduct annotation){
        if(annotation.notEmpty() && value.trim().isEmpty()) throw new IllegalArgumentException(fieldName + " cannot be empty.");
    }

    private static void validateDouble(String fieldName, Double value, ValidateProduct annotation){
        if(value < annotation.minValue()) throw new IllegalArgumentException(fieldName + " must be at least " + annotation.minValue());
    }
    private static void validateInt(String fieldName, Integer value, ValidateProduct annotation){
        if(value < annotation.minValue()) throw new IllegalArgumentException(fieldName + " must be at least " + annotation.minValue());
    }
    private static void validateLocalDate(String fieldName, LocalDate value, ValidateProduct annotation){
        if(value.isBefore(LocalDate.now()) && !annotation.allowPastDate()) throw new ExpiredProductException(fieldName + " cannot be in the past.");
    }


}
