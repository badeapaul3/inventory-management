package com.inventory.model;

import com.inventory.validation.ValidateProduct;

import java.time.LocalDate;

/**
 * Immutable Product entity using Java Record.
 */
public record Product(int id, String name, double price, int stock, @ValidateProduct LocalDate expirationDate) { }
