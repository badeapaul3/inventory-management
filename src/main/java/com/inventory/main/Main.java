package com.inventory.main;

import com.inventory.dao.ProductDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.discount.DiscountService;
import com.inventory.discount.DiscountStrategy;
import com.inventory.discount.FlatDiscountStrategy;
import com.inventory.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        try (ProductDAO productDAO = new ProductDAO()) {
            // Valid product
            Product product1 = new Product(0, "Test Product", 20.0, 50, LocalDate.of(2025, 12, 31));
            productDAO.insertOrUpdateProduct(product1, true);
            System.out.println("Inserted product1: " + product1);
            System.out.println("Products after valid insert: " + productDAO.getAllProducts(false));

            // Potentially expired product (future date)
            Product product2 = new Product(0, "Expired Milk", 5.0, 10, LocalDate.of(2026, 3, 1));
            productDAO.insertOrUpdateProduct(product2, true);
            System.out.println("Inserted product2: " + product2);

            // Test expired product (past date)
            Product product3 = new Product(0, "Old Cheese", 8.0, 30, LocalDate.of(2025, 3, 1));
            productDAO.insertOrUpdateProduct(product3, true);
            System.out.println("Inserted product3: " + product3);

            // Discount test
            DiscountService discountService = new DiscountService();
            DiscountStrategy flatDiscount = new FlatDiscountStrategy(BigDecimal.valueOf(25));
            Product discountedProduct = discountService.applyDiscount(
                    productDAO.getAllProducts(false).get(0), flatDiscount
            );
            System.out.println("After $25 flat discount: " + discountedProduct);

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}