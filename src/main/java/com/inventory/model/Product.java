package com.inventory.model;

import com.inventory.validation.ValidateProduct;

import java.time.LocalDate;

/**
 * Immutable Product entity using Java Record.
 */
public record Product(@ValidateProduct(notNull = false, minValue = -1) int id,
                      @ValidateProduct(notEmpty = true) String name,
                      @ValidateProduct(minValue = 0.01) double price,
                      @ValidateProduct(minValue = 0) int stock,
                      @ValidateProduct(allowPastDate = false) LocalDate expirationDate ,
                      @ValidateProduct(notNull = true) boolean discounted){ }
