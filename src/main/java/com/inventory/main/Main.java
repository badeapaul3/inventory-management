package com.inventory.main;

import com.inventory.dao.ProductDAO;
import com.inventory.database.DatabaseInitializer;
import com.inventory.model.Product;

import java.sql.SQLException;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initializeDatabase();
        try (ProductDAO productDAO = new ProductDAO()) {
            Product product = new Product(0, "Test", 10.0, 20, LocalDate.of(2025, 12, 31));
            productDAO.insertOrUpdateProduct(product);
            System.out.println(productDAO.getAllProducts());
            productDAO.deleteProduct(20); // Delete by ID
        } catch (SQLException e) {
            e.printStackTrace();
        } //DAO autoclosed


    }
}
