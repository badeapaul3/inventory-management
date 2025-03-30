package com.inventory.dao;

import com.inventory.database.DatabaseManager;
import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;
import com.inventory.validation.ProductValidator;

import javax.management.relation.RoleInfoNotFoundException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * DAO Pattern: Handles database operations for Product.
 */
public class ProductDAO implements AutoCloseable{
    private final ReentrantLock lock = new ReentrantLock(); // Ensures thread safety
    private final Connection connection;

    public ProductDAO() {
        try {
            this.connection = DatabaseManager.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }

    /**
     * Inserts a product or updates stock if it already exists.
     * Throws ExpiredProductException if the product is expired.
     */
    public void insertOrUpdateProduct(Product product, boolean throwOnExpired) {
        lock.lock();
        try {
            ProductValidator.validateProduct(product);

            // Check for expiration
            if (product.expirationDate().isBefore(LocalDate.now())) {
                String message = "Product '" + product.name() + "' is expired (expiration: " + product.expirationDate() + ").";
                if (throwOnExpired) {
                    throw new ExpiredProductException(message);
                } else {
                    System.err.println("Warning: " + message + " Skipping insertion.");
                    return; // Skip insertion
                }
            }

            String checkQuery = "SELECT id, stock FROM Product WHERE name = ? AND price = ? AND expiration_date = ?";
            String insertQuery = "INSERT INTO Product (name, price, stock, expiration_date) VALUES (?, ?, ?, ?)";
            String updateQuery = "UPDATE Product SET stock = stock + ? WHERE id = ?";

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, product.name());
                checkStmt.setDouble(2, product.price());
                checkStmt.setString(3, product.expirationDate().toString());

                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int existingId = rs.getInt("id");
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, product.stock());
                        updateStmt.setInt(2, existingId);
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, product.name());
                        insertStmt.setDouble(2, product.price());
                        insertStmt.setInt(3, product.stock());
                        insertStmt.setString(4, product.expirationDate().toString());
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error inserting or updating product", e);
        } finally {
            lock.unlock();
        }
    }

    public void insertOrUpdateProduct(Product product){
        insertOrUpdateProduct(product,true);
    }

    /**
     * Retrieves all products from the database.
     */
    public List<Product> getAllProducts(boolean throwOnExpired) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date FROM Product"; // 🟢 Correct order (Stock first)

        lock.lock();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                // Convert the expirationDate string to LocalDate
                LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"), // 🟢 Correct order (Stock first)
                        expirationDate // 🟢 Correct order (ExpirationDate second)
                );

                if(throwOnExpired && expirationDate.isBefore(LocalDate.now())) throw new ExpiredProductException("Product '" + product.name() + "' is expired since " + expirationDate);
                products.add(product);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving products", e);
        } finally {
            lock.unlock();
        }
        return products;
    }

    /**
     * Updates an existing prod by id with new values
     */

    public void updateProduct(Product product){
        lock.lock();
        try{
            ProductValidator.validateProduct(product);
            if(product.id() <=0){
                throw new IllegalArgumentException("Cannot update product with invalid id " + product.id());
            }
            String updateQuery = "update Product set name = ?, price = ?, stock = ?, expiration_date = ? where id = ?";
            try(PreparedStatement stmt = connection.prepareStatement(updateQuery)){
                stmt.setString(1,product.name());
                stmt.setDouble(2, product.price());
                stmt.setInt(3, product.stock());
                stmt.setString(4, product.expirationDate().toString());
                stmt.setInt(5, product.id());

                int rowsAffected = stmt.executeUpdate();
                if(rowsAffected == 0) throw new SQLException("No product found with ID: " + product.id());
            }
        }catch (SQLException e){
            throw new RuntimeException("Error updating product", e);
        }finally {
            lock.unlock();
        }
    }




    public void deleteProduct(int id) {
        lock.lock();

        if (id <= 0) {
            throw new IllegalArgumentException("Cannot delete product with invalid ID: " + id);
        }

        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM Product WHERE id = ?")) {
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No product found with ID: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        } finally {
            lock.unlock();
        }
    }

    public List<Product> findProductsByName(String name){
        if(name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name cannot be null or empty");

        List<Product> products = new ArrayList<>();
        String query = "Select id, name, price, stock, expiration_date from Product where name like ?";

        lock.lock();
        try(PreparedStatement stmt = connection.prepareStatement(query)){
            stmt.setString(1,"%" + name.trim() + "%");

            try(ResultSet rs = stmt.executeQuery()){
                while (rs.next()){
                    LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                    Product product = new Product(
                      rs.getInt("id"),
                      rs.getString("name"),
                      rs.getDouble("price"),
                      rs.getInt("stock"),
                      expirationDate
                    );
                    products.add(product);
                }
            }
        }catch (SQLException e){
            throw new RuntimeException("Error finding products by name", e);
        }finally {
            lock.unlock();
        }
        return products;
    }

    public List<Product> findProductsExpiringBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }

        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date FROM Product WHERE expiration_date < ?";

        lock.lock();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, date.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            expirationDate
                    );
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products expiring before date", e);
        } finally {
            lock.unlock();
        }
        return products;
    }



    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }







}
