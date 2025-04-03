package com.inventory.dao;

import com.inventory.exception.ExpiredProductException;
import com.inventory.model.Product;
import com.inventory.validation.ProductValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ProductDAO implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAO.class);
    private final ReentrantLock lock = new ReentrantLock();
    private final Connection connection;
    private final HistoryDAO historyDAO;

    public ProductDAO(Connection connection, HistoryDAO historyDAO) {
        this.connection = connection;
        this.historyDAO = historyDAO;
    }

    public void insertOrUpdateProduct(Product product) {
        lock.lock();
        try {
            ProductValidator.validateProduct(product);
            logger.debug("Inserting or updating product: {}", product);

            String checkQuery = "SELECT id, stock FROM Product WHERE name = ? AND price = ? AND expiration_date = ?";
            String insertQuery = "INSERT INTO Product (name, price, stock, expiration_date, discounted, category_id, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
            String updateQuery = "UPDATE Product SET stock = stock + ?, discounted = ?, category_id = ?, supplier_id = ? WHERE id = ?";

            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, product.name());
                checkStmt.setDouble(2, product.price());
                checkStmt.setString(3, product.expirationDate().toString());
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int existingId = rs.getInt("id");
                    int oldStock = rs.getInt("stock");
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, product.stock());
                        updateStmt.setBoolean(2, product.discounted());
                        updateStmt.setObject(3, product.categoryId(), Types.INTEGER);
                        updateStmt.setObject(4, product.supplierId(), Types.INTEGER);
                        updateStmt.setInt(5, existingId);
                        updateStmt.executeUpdate();
                        historyDAO.logProductHistory(existingId, "UPDATE", "stock: " + oldStock, "stock: " + (oldStock + product.stock()));
                        logger.info("Updated product ID: {}, new stock: {}", existingId, oldStock + product.stock());
                    }
                } else {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, product.name());
                        insertStmt.setDouble(2, product.price());
                        insertStmt.setInt(3, product.stock());
                        insertStmt.setString(4, product.expirationDate().toString());
                        insertStmt.setBoolean(5, product.discounted());
                        insertStmt.setObject(6, product.categoryId(), Types.INTEGER);
                        insertStmt.setObject(7, product.supplierId(), Types.INTEGER);
                        insertStmt.executeUpdate();

                        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            int newId = generatedKeys.getInt(1);
                            historyDAO.logProductHistory(newId, "ADD", null, "price: " + product.price() + ", stock: " + product.stock());
                            logger.info("Inserted new product ID: {}", newId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error inserting or updating product: {}", e.getMessage(), e);
            throw new RuntimeException("Error inserting or updating product", e);
        } catch (IllegalArgumentException | ExpiredProductException e) {
            logger.warn("Validation failed for product: {}", e.getMessage());
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public List<Product> getAllProducts(boolean throwOnExpired) {
        List<Product> products = new ArrayList<>();
        String query = "SELECT id, name, price, stock, expiration_date, discounted, category_id, supplier_id FROM Product";

        lock.lock();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            logger.debug("Retrieving all products, throwOnExpired: {}", throwOnExpired);
            while (rs.next()) {
                LocalDate expirationDate = LocalDate.parse(rs.getString("expiration_date"));
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        expirationDate,
                        rs.getBoolean("discounted"),
                        rs.getObject("category_id") != null ? rs.getInt("category_id") : null,
                        rs.getObject("supplier_id") != null ? rs.getInt("supplier_id") : null
                );

                if (throwOnExpired && expirationDate.isBefore(LocalDate.now())) {
                    logger.warn("Expired product found: {}", product);
                    throw new ExpiredProductException("Product '" + product.name() + "' is expired (expiration: " + expirationDate + ").");
                }
                products.add(product);
            }
            logger.info("Retrieved {} products.", products.size());
        } catch (SQLException e) {
            logger.error("Error retrieving products: {}", e.getMessage(), e);
            throw new RuntimeException("Error retrieving products", e);
        } finally {
            lock.unlock();
        }
        return products;
    }

    public void updateProduct(Product product) throws SQLException {
        lock.lock();
        try {
            ProductValidator.validateProduct(product);
            if (product.id() <= 0) {
                logger.warn("Invalid product ID for update: {}", product.id());
                throw new IllegalArgumentException("Cannot update product with invalid ID: " + product.id());
            }

            String selectQuery = "SELECT price, stock FROM Product WHERE id = ?";
            String updateQuery = "UPDATE Product SET name = ?, price = ?, stock = ?, expiration_date = ?, discounted = ?, category_id = ?, supplier_id = ? WHERE id = ?";

            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, product.id());
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    logger.warn("Product not found for update, ID: {}", product.id());
                    throw new SQLException("No product found with ID: " + product.id());
                }

                String oldValue = "price: " + rs.getDouble("price") + ", stock: " + rs.getInt("stock");
                String newValue = "price: " + product.price() + ", stock: " + product.stock();

                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setString(1, product.name());
                    updateStmt.setDouble(2, product.price());
                    updateStmt.setInt(3, product.stock());
                    updateStmt.setString(4, product.expirationDate().toString());
                    updateStmt.setBoolean(5, product.discounted());
                    updateStmt.setObject(6, product.categoryId(), Types.INTEGER);
                    updateStmt.setObject(7, product.supplierId(), Types.INTEGER);
                    updateStmt.setInt(8, product.id());
                    int rowsAffected = updateStmt.executeUpdate();
                    if (rowsAffected > 0) {
                        historyDAO.logProductHistory(product.id(), "UPDATE", oldValue, newValue);
                        logger.info("Product updated, ID: {}", product.id());
                    }
                }
            }
        } catch (IllegalArgumentException | ExpiredProductException e) {
            logger.warn("Validation failed for update: {}", e.getMessage());
            throw e;
        } finally {
            lock.unlock();
        }
    }

    public void deleteProduct(int id) throws SQLException {
        lock.lock();
        try {
            if (id <= 0) {
                logger.warn("Invalid product ID for delete: {}", id);
                throw new IllegalArgumentException("Cannot delete product with invalid ID: " + id);
            }
            String deleteQuery = "DELETE FROM Product WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(deleteQuery)) {
                stmt.setInt(1, id);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    logger.warn("Product not found for delete, ID: {}", id);
                    throw new SQLException("No product found with ID: " + id);
                }
                historyDAO.logProductHistory(id, "DELETE", null, null);
                logger.info("Product deleted, ID: {}", id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void adjustStock(int id, int amount) {
        lock.lock();
        try {
            if (id <= 0) {
                logger.warn("Invalid product ID for stock adjust: {}", id);
                throw new IllegalArgumentException("Cannot adjust stock for invalid ID: " + id);
            }

            String selectQuery = "SELECT stock FROM Product WHERE id = ?";
            int currentStock;
            try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
                selectStmt.setInt(1, id);
                ResultSet rs = selectStmt.executeQuery();
                if (!rs.next()) {
                    logger.warn("Product not found for stock adjust, ID: {}", id);
                    throw new SQLException("No product found with ID: " + id);
                }
                currentStock = rs.getInt("stock");
            }

            int newStock = currentStock + amount;
            if (newStock < 0) {
                logger.warn("Stock adjustment would go below 0, ID: {}, current: {}, change: {}", id, currentStock, amount);
                throw new IllegalStateException("Stock cannot go below 0. Current: " + currentStock + ", Attempted change: " + amount);
            }

            String updateQuery = "UPDATE Product SET stock = ? WHERE id = ?";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, newStock);
                updateStmt.setInt(2, id);
                updateStmt.executeUpdate();
                historyDAO.logProductHistory(id, "STOCK_ADJUST", "stock: " + currentStock, "stock: " + newStock);
                logger.info("Stock adjusted, ID: {}, from {} to {}", id, currentStock, newStock);
            }
        } catch (SQLException e) {
            logger.error("Error adjusting stock: {}", e.getMessage(), e);
            throw new RuntimeException("Error adjusting stock", e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() throws SQLException {
        logger.debug("Closing ProductDAO.");
        // Connection closed by caller (Main)
    }
}