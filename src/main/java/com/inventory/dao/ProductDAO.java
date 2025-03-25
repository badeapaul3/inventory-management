package com.inventory.dao;

import com.inventory.database.DatabaseManager;
import com.inventory.model.Product;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO Pattern: Handles database operations for Product.
 */
public class ProductDAO {

    public void addProduct(Product product) {
        String sql = "INSERT INTO Product (name, price, stock, expiration_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.name());
            pstmt.setDouble(2, product.price());
            pstmt.setInt(3, product.stock());
            pstmt.setString(4, product.expirationDate().toString());

            pstmt.executeUpdate();
            System.out.println("✅ Product added: " + product);

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error adding product", e);
        }
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Product";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        LocalDate.parse(rs.getString("expiration_date"))
                ));
            }

        } catch (SQLException e) {
            throw new RuntimeException("❌ Error fetching products", e);
        }
        return products;
    }
}
