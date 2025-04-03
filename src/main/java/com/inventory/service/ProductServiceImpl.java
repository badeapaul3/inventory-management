package com.inventory.service;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.HistoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.exception.ProductNotFoundException;
import com.inventory.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final SupplierDAO supplierDAO;
    private final HistoryDAO historyDAO;

    public ProductServiceImpl(ProductDAO productDAO, CategoryDAO categoryDAO, SupplierDAO supplierDAO, HistoryDAO historyDAO) {
        this.productDAO = productDAO;
        this.categoryDAO = categoryDAO;
        this.supplierDAO = supplierDAO;
        this.historyDAO = historyDAO;
    }

    @Override
    public void addProduct(Product product) {
        if (product == null) {
            logger.warn("Attempted to add null product.");
            throw new IllegalArgumentException("Product cannot be null");
        }
        productDAO.insertOrUpdateProduct(product);
        logger.info("Product added via service: {}", product);
    }

    @Override
    public List<Product> getAllProducts() {
        List<Product> products = productDAO.getAllProducts(false);
        logger.debug("Retrieved all products, count: {}", products.size());
        return products;
    }

    @Override
    public void updateProduct(Product product) {
        if (product == null) {
            logger.warn("Attempted to update null product.");
            throw new IllegalArgumentException("Product cannot be null");
        }
        try {
            productDAO.updateProduct(product);
            logger.info("Product updated via service: {}", product);
        } catch (SQLException e) {
            if (e.getMessage().contains("No product found")) {
                logger.warn("Product not found for update, ID: {}", product.id());
                throw new ProductNotFoundException("Product with ID " + product.id() + " not found");
            }
            logger.error("Database error updating product: {}", e.getMessage(), e);
            throw new RuntimeException("Database error updating product", e);
        }
    }

    @Override
    public void deleteProduct(int id) {
        try {
            productDAO.deleteProduct(id);
            logger.info("Product deleted via service, ID: {}", id);
        } catch (SQLException e) {
            if (e.getMessage().contains("No product found")) {
                logger.warn("Product not found for delete, ID: {}", id);
                throw new ProductNotFoundException("Product with ID " + id + " not found");
            }
            logger.error("Database error deleting product: {}", e.getMessage(), e);
            throw new RuntimeException("Database error deleting product", e);
        }
    }

    @Override
    public void adjustStock(int id, int amount) {
        if (id <= 0) {
            logger.warn("Invalid product ID for stock adjust: {}", id);
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        productDAO.adjustStock(id, amount);
        logger.info("Stock adjusted via service, ID: {}, amount: {}", id, amount);
    }

    @Override
    public List<Product> findProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            logger.warn("Invalid search name: {}", name);
            throw new IllegalArgumentException("Search name cannot be empty");
        }
        List<Product> results = productDAO.getAllProducts(false).stream()
                .filter(p -> p.name().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
        logger.debug("Found {} products matching name: {}", results.size(), name);
        return results;
    }

    @Override
    public List<Product> findProductsExpiringBefore(LocalDate date) {
        if (date == null) {
            logger.warn("Invalid date for expiry search: null");
            throw new IllegalArgumentException("Date cannot be null");
        }
        List<Product> results = productDAO.getAllProducts(false).stream()
                .filter(p -> p.expirationDate().isBefore(date))
                .collect(Collectors.toList());
        logger.debug("Found {} products expiring before: {}", results.size(), date);
        return results;
    }

    @Override
    public void applyDiscount(int id) {
        if (id <= 0) {
            logger.warn("Invalid product ID for discount: {}", id);
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        Product product = findProductById(id);
        if (product == null) {
            logger.warn("Product not found for discount, ID: {}", id);
            throw new IllegalStateException("Product not found with ID: " + id);
        }
        if (!product.discounted()) {
            Product updatedProduct = new Product(
                    product.id(), product.name(), product.price() * 0.9, product.stock(),
                    product.expirationDate(), true, product.categoryId(), product.supplierId()
            );
            try {
                productDAO.updateProduct(updatedProduct);
                logger.info("Discount applied via service, ID: {}", id);
            } catch (SQLException e) {
                if (e.getMessage().contains("No product found")) {
                    logger.warn("Product not found during discount update, ID: {}", id);
                    throw new ProductNotFoundException("Product with ID " + product.id() + " not found");
                }
                logger.error("Database error applying discount: {}", e.getMessage(), e);
                throw new RuntimeException("Database error updating product", e);
            }
        } else {
            logger.debug("Product already discounted, ID: {}", id);
        }
    }

    @Override
    public void adjustStockForExpired() {
        List<Product> products = productDAO.getAllProducts(false);
        int adjustedCount = 0;
        for (Product p : products) {
            if (p.expirationDate().isBefore(LocalDate.now()) && p.stock() > 0) {
                productDAO.adjustStock(p.id(), -p.stock());
                adjustedCount++;
            }
        }
        logger.info("Adjusted stock for {} expired products.", adjustedCount);
    }

    @Override
    public int addCategory(String name) {
        int id = categoryDAO.insertCategory(name);
        logger.info("Category added via service, ID: {}", id);
        return id;
    }

    @Override
    public int addSupplier(String name, String contactInfo) {
        int id = supplierDAO.insertSupplier(name, contactInfo);
        logger.info("Supplier added via service, ID: {}", id);
        return id;
    }

    private Product findProductById(int id) {
        Product product = productDAO.getAllProducts(false).stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElse(null);
        logger.debug("Find product by ID: {}, found: {}", id, product != null);
        return product;
    }
}