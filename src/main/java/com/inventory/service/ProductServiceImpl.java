package com.inventory.service;

import com.inventory.dao.CategoryDAO;
import com.inventory.dao.HistoryDAO;
import com.inventory.dao.ProductDAO;
import com.inventory.dao.SupplierDAO;
import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ProductServiceImpl implements ProductService {
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
            throw new IllegalArgumentException("Product cannot be null");
        }
        productDAO.insertOrUpdateProduct(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts(false); // No exception on expired for listing
    }

    @Override
    public void updateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        productDAO.updateProduct(product);
    }

    @Override
    public void deleteProduct(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        productDAO.deleteProduct(id);
    }

    @Override
    public void adjustStock(int id, int amount) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        productDAO.adjustStock(id, amount);
    }

    @Override
    public List<Product> findProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Search name cannot be empty");
        }
        return productDAO.getAllProducts(false).stream()
                .filter(p -> p.name().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findProductsExpiringBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return productDAO.getAllProducts(false).stream()
                .filter(p -> p.expirationDate().isBefore(date))
                .collect(Collectors.toList());
    }

    @Override
    public void applyDiscount(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        Product product = findProductById(id); // Helper method below
        if (product == null) {
            throw new IllegalStateException("Product not found with ID: " + id);
        }
        if (!product.discounted()) {
            Product updatedProduct = new Product(
                    product.id(), product.name(), product.price() * 0.9, product.stock(),
                    product.expirationDate(), true, product.categoryId(), product.supplierId()
            );
            productDAO.updateProduct(updatedProduct);
        }
    }

    @Override
    public void adjustStockForExpired() {
        List<Product> products = productDAO.getAllProducts(false);
        for (Product p : products) {
            if (p.expirationDate().isBefore(LocalDate.now()) && p.stock() > 0) {
                productDAO.adjustStock(p.id(), -p.stock()); // Clear expired stock
            }
        }
    }

    @Override
    public int addCategory(String name) {
        return categoryDAO.insertCategory(name);
    }

    @Override
    public int addSupplier(String name, String contactInfo) {
        return supplierDAO.insertSupplier(name, contactInfo);
    }

    // Helper method to find product by ID
    private Product findProductById(int id) {
        return productDAO.getAllProducts(false).stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElse(null);
    }
}