package com.inventory.model;

import java.time.LocalDate;

/**
 * Immutable Product entity using Java Record.
 */
public record Product(int id, String name, double price, int stock, LocalDate expirationDate) { }
