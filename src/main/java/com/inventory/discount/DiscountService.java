package com.inventory.discount;

import com.inventory.dao.ProductDAO;
import com.inventory.model.Product;
import com.inventory.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

public class DiscountService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);
    private final ProductDAO productDAO;

    public DiscountService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Product applyDiscount(Product product, DiscountStrategy strategy) {
        if (product == null || strategy == null) {
            logger.warn("Null product or strategy provided for discount.");
            throw new IllegalArgumentException("Product and DiscountStrategy cannot be null.");
        }

        BigDecimal originalPrice = BigDecimal.valueOf(product.price());
        BigDecimal discountedPrice = strategy.applyDiscount(originalPrice)
                .setScale(2, RoundingMode.HALF_UP);

        if (discountedPrice.compareTo(BigDecimal.ZERO) < 0) {
            logger.debug("Discounted price for product ID: {} capped at 0 from {}", product.id(), discountedPrice);
            discountedPrice = BigDecimal.ZERO;
        }

        Product discountedProduct = new Product(
                product.id(), product.name(), discountedPrice.doubleValue(), product.stock(),
                product.expirationDate(), true, product.categoryId(), product.supplierId()
        );
        logger.debug("Applied discount to product ID: {}, new price: {}", product.id(), discountedProduct.price());
        return discountedProduct;
    }

    public void applyAndSaveDiscount(Product product, DiscountStrategy strategy) {
        if (productDAO == null) {
            logger.error("ProductDAO not initialized in DiscountService.");
            throw new IllegalStateException("ProductDAO not initialized. Use constructor with ProductDAO.");
        }

        Product discountedProduct = applyDiscount(product, strategy);

        try {
            productDAO.updateProduct(discountedProduct);
            logger.info("Saved discounted product ID: {}", discountedProduct.id());
        } catch (SQLException e) {
            if (e.getMessage().contains("No product found")) {
                logger.warn("Product not found for discount save, ID: {}", product.id());
                throw new ProductNotFoundException("Product with ID " + product.id() + " not found");
            }
            logger.error("Database error saving discounted product: {}", e.getMessage(), e);
            throw new RuntimeException("Database error updating product", e);
        }
    }
}