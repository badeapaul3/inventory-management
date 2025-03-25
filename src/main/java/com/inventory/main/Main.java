package com.inventory.main;

import com.inventory.database.DatabaseInitializer;
import com.inventory.dao.ProductDAO;
import com.inventory.factory.ProductFactory;
import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.List;

/**
 * Main entry point: Initializes DB and tests DAO operations.
 */
public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase(); // Create tables if needed

        ProductDAO productDAO = new ProductDAO();

        // Add products using the Factory Pattern
        Product milk = ProductFactory.createProduct("Milk", 2.99, 30, LocalDate.of(2025, 4, 10));
        Product bread = ProductFactory.createProduct("Bread", 1.49, 50, LocalDate.of(2025, 4, 5));

        productDAO.addProduct(milk);
        productDAO.addProduct(bread);

        // Fetch and print all products
        List<Product> products = productDAO.getAllProducts();
        products.forEach(System.out::println);
    }
}
