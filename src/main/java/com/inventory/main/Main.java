package com.inventory.main;

import com.inventory.dao.ProductDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;

import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        try (ProductDAO productDAO = new ProductDAO()) {
            // Valid product (future expiration)
            Product product1 = new Product(0, "Test Product", 10.0, 20, LocalDate.of(2025, 12, 31));
            productDAO.insertOrUpdateProduct(product1);
            System.out.println("Product 1 added: " + productDAO.getAllProducts(false));

            // Expired product (should throw exception on insert)
            Product product2 = new Product(0, "Expired Milk", 5.0, 10, LocalDate.of(2025, 3, 1)); // Before March 29, 2025
            productDAO.insertOrUpdateProduct(product2);

        } catch (ExpiredProductException e) {
            System.err.println("Expired product error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        // Test getAllProducts with throwOnExpired
        try (ProductDAO productDAO = new ProductDAO()) {
            System.out.println("All products (no throw): " + productDAO.getAllProducts(false));
            System.out.println("All products (throw on expired): " + productDAO.getAllProducts(true));
        } catch (ExpiredProductException e) {
            System.err.println("Expired product error on retrieval: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}