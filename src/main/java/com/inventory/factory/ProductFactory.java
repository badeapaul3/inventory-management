package com.inventory.factory;

import com.inventory.model.Product;

import java.time.LocalDate;

/**
 * Factory Pattern: Encapsulates Product object creation.
 */
public class ProductFactory {

    public static Product createProduct(String name, double price, int stock, LocalDate expirationDate) {
        return new Product(0, name, price, stock, expirationDate); // ID set to 0 (auto-incremented in DB)
    }
}
