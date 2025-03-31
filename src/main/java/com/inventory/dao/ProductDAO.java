package com.inventory.dao;

import com.inventory.database.DatabaseManager;
import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;
import com.inventory.validation.ProductValidator;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ProductDAO implements AutoCloseable {
    private final ReentrantLock lock = new ReentrantLock();
    private final Connection connection;

    public ProductDAO() {
        try {
            this.connection = DatabaseManager.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection", e);
        }
    }

    public void insertOrUpdateProduct(Product product, boolean throwOnExpired) {
        lock.lock();
        try {
            ProductValidator.validateProduct(product);

            if (product.expirationDate().isBefore(LocalDate.now()) && !throwOnExpired) {
                String message = "Product '" + product.name() + "' is expired (expiration: " + product.expirationDate() + ").";
                if (throwOnExpired) {
                    throw new ExpiredProductException(message);
                } else {
                    System.err.println("Warning: " + message + " Skipping insertion.");
                    return;
                }
            }

            String checkQuery = "SELECT id, stock FROM Product WHERE name = ? AND price = ? AND expiration_date = ?";
            String insertQuery = "INSERT INTO Product (name, price, stock, expiration_date, discounted) VALUES (?, ?, ?, ?, ?)";
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
                        insertStmt.setBoolean(5, product.discounted());
                        insertStmt.executeUpdate();
                    }
                }
            }
        }catch (SQLException e) {
            throw new RuntimeException("Error inserting or updating product", e);
        }catch (IllegalArgumentException | ExpiredProductException e){
            throw e;
        }finally {
            lock.unlock();
        }
    }

    public void insertOrUpdateProduct(Product product) {
        insertOrUpdateProduct(product, true);
    }

    public List<Product> getAllProducts(boolean throwOnExpired) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date, discounted FROM Product";

        lock.lock();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        expirationDate,
                        rs.getBoolean("discounted")
                );

                if (throwOnExpired && expirationDate.isBefore(LocalDate.now())) {
                    throw new ExpiredProductException("Product '" + product.name() + "' is expired (expiration: " + expirationDate + ").");
                }
                products.add(product);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving products", e);
        } finally {
            lock.unlock();
        }
        return products;
    }

    public void updateProduct(Product product) {
        lock.lock();
        try {
            ProductValidator.validateProduct(product);

            if (product.id() <= 0) {
                throw new IllegalArgumentException("Cannot update product with invalid ID: " + product.id());
            }

            String updateQuery = "UPDATE Product SET name = ?, price = ?, stock = ?, expiration_date = ?, discounted = ? WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setString(1, product.name());
                stmt.setDouble(2, product.price());
                stmt.setInt(3, product.stock());
                stmt.setString(4, product.expirationDate().toString());
                stmt.setBoolean(5, product.discounted());
                stmt.setInt(6, product.id());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No product found with ID: " + product.id());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating product", e);
        } catch (IllegalArgumentException | ExpiredProductException e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public void deleteProduct(int id) {
        lock.lock();
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Cannot delete product with invalid ID: " + id);
            }

            String deleteQuery = "DELETE FROM Product WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new SQLException("No product found with ID: " + id);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting product", e);
        } finally {
            lock.unlock();
        }
    }

    public List<Product> findProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty.");
        }

        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date, discounted FROM Product WHERE name LIKE ?";

        lock.lock();
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + name.trim() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                    Product product = new Product(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            expirationDate,
                            rs.getBoolean("discounted")
                    );
                    products.add(product);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding products by name", e);
        } finally {
            lock.unlock();
        }
        return products;
    }

    public List<Product> findProductsExpiringBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null.");
        }

        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date, discounted FROM Product WHERE expiration_date < ?";

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
                            expirationDate,
                            rs.getBoolean("discounted")
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

    public void adjustStock(int id, int amount) {
        lock.lock();
        try {
            if (id <= 0) {
                throw new IllegalArgumentException("Cannot adjust stock for invalid ID: " + id);
            }

            String selectQuery = "SELECT stock FROM Product WHERE id = ?";
            int currentStock;
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, id);
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    throw new SQLException("No product found with ID: " + id);
                }
                currentStock = rs.getInt("stock");
            }

            int newStock = currentStock + amount;
            if (newStock < 0) {
                throw new IllegalStateException("Stock cannot go below 0. Current: " + currentStock + ", Attempted change: " + amount);
            }

            String updateQuery = "UPDATE Product SET stock = ? WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, newStock);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error adjusting stock", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}