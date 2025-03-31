package com.inventory.factory;

import com.inventory.model.Product;

import java.time.LocalDate;

/**
 * Factory Pattern: Encapsulates Product object creation.
 */
public class ProductFactory {

    public static Product createProduct(int id, String name, double price, int stock, LocalDate expirationDate, boolean discounted, int categoryId, int supplierId) {
        return new Product(id, name, price, stock, expirationDate, discounted, categoryId, supplierId); // ID set to 0 (auto-incremented in DB)
    }
}
