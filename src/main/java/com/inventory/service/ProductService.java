package com.inventory.service;

import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.List;

public interface ProductService {
    void addProduct(Product product);
    List<Product> getAllProducts();
    void updateProduct(Product product);
    void deleteProduct(int id);
    void adjustStock(int id, int amount);
    List<Product> findProductsByName(String name);
    List<Product> findProductsExpiringBefore(LocalDate date);
    void applyDiscount(int id);
    void adjustStockForExpired();

    // New methods for categories and suppliers
    int addCategory(String name);
    int addSupplier(String name, String contactInfo);
}