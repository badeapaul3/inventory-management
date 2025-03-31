package com.inventory.service;

import com.inventory.dao.ProductDAO;
import com.inventory.discount.DiscountManager;
import com.inventory.discount.DiscountService;
import com.inventory.discount.DiscountStrategy;
import com.inventory.model.Product;

import java.time.LocalDate;
import java.util.List;
public class ProductServiceImpl implements ProductService{
    private final ProductDAO productDAO;
    private final DiscountService discountService;
    private final DiscountManager discountManager;

    public ProductServiceImpl(ProductDAO productDAO) {
        this.productDAO = productDAO;
        this.discountService = new DiscountService(productDAO);
        this.discountManager = new DiscountManager(productDAO);
    }


    @Override
    public void addProduct(Product product) {
        if(product == null){
            throw new IllegalArgumentException("Product cannot be null");
        }
        productDAO.insertOrUpdateProduct(product);
    }

    @Override
    public List<Product> getAllProducts() {
        return productDAO.getAllProducts(false); // false to skip expired check for simplicity
    }

    @Override
    public void deleteProduct(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        productDAO.deleteProduct(id);
    }

    @Override
    public void updateProduct(Product product) {
        if (product == null || product.id() <= 0) {
            throw new IllegalArgumentException("Invalid product or ID for update");
        }
        productDAO.updateProduct(product);
    }
    @Override
    public void adjustStock(int id, int amount) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        productDAO.adjustStock(id, amount);
    }

    @Override
    public Product applyManualDiscount(int id, DiscountStrategy strategy) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        if (strategy == null) {
            throw new IllegalArgumentException("Discount strategy cannot be null");
        }
        Product product = findProductById(id);
        Product discountedProduct = discountService.applyDiscount(product, strategy);
        productDAO.updateProduct(discountedProduct);
        return discountedProduct;
    }

    @Override
    public int applyDynamicDiscounts() {
        return discountManager.applyDynamicDiscounts();
    }

    @Override
    public Product applyDynamicDiscount(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid product ID: " + id);
        }
        return discountManager.applyDynamicDiscount(id);
    }

    @Override
    public List<Product> findProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        return productDAO.findProductsByName(name);
    }

    @Override
    public List<Product> findProductsExpiringBefore(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return productDAO.findProductsExpiringBefore(date);
    }

    private Product findProductById(int id) {
        return getAllProducts().stream()
                .filter(p -> p.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No product found with ID: " + id));
    }
}
